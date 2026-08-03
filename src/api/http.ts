export interface ApiRequestOptions extends RequestInit {
  fallbackLabel?: string;
  authPortal?: AuthPortal;
  skipAuth?: boolean;
}

export interface FileDownload {
  blob: Blob;
  filename?: string;
}

interface ApiEnvelope<T> {
  code?: number | string;
  success?: boolean;
  message?: string;
  msg?: string;
  data?: T;
  result?: T;
  rows?: T;
  list?: T;
  token?: string;
  accessToken?: string;
}

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/$/, '');
export type AuthPortal = 'admin' | 'student';

const legacyAuthTokenKey = 'jiaoxuepeiyu_token';
const legacyAuthUserKey = 'jiaoxuepeiyu_user';
const authStorageKeys: Record<AuthPortal, { token: string; user: string }> = {
  admin: {
    token: 'jiaoxuepeiyu_admin_token',
    user: 'jiaoxuepeiyu_admin_user'
  },
  student: {
    token: 'jiaoxuepeiyu_student_token',
    user: 'jiaoxuepeiyu_student_user'
  }
};

let redirectingToLogin = false;

function buildUrl(path: string): string {
  if (/^https?:\/\//i.test(path)) {
    return path;
  }

  return `${apiBaseUrl}${path.startsWith('/') ? path : `/${path}`}`;
}

function inferPortal(path: string): AuthPortal | undefined {
  const normalized = path.replace(/^https?:\/\/[^/]+/i, '');

  if (/^\/?(api\/)?(admin|auth\/admin)(\/|$)/i.test(normalized)) {
    return 'admin';
  }

  if (/^\/?(api\/)?(student|auth\/student)(\/|$)/i.test(normalized)) {
    return 'student';
  }

  return undefined;
}

function getStoredToken(portal?: AuthPortal): string {
  if (!portal) {
    return globalThis.localStorage?.getItem(legacyAuthTokenKey) || '';
  }

  return globalThis.localStorage?.getItem(authStorageKeys[portal].token) || globalThis.localStorage?.getItem(legacyAuthTokenKey) || '';
}

export function hasAuthSession(portal: AuthPortal): boolean {
  return Boolean(getStoredToken(portal));
}

function getStoredUserId(portal?: AuthPortal): string {
  const storedUser = portal
    ? globalThis.localStorage?.getItem(authStorageKeys[portal].user) || globalThis.localStorage?.getItem(legacyAuthUserKey)
    : globalThis.localStorage?.getItem(legacyAuthUserKey);

  if (!storedUser) {
    return '';
  }

  try {
    const user = JSON.parse(storedUser) as { id?: number | string; userId?: number | string; studentId?: number | string };
    return String(user.id ?? user.userId ?? user.studentId ?? '');
  } catch {
    return '';
  }
}

function unwrapResponse<T>(payload: ApiEnvelope<T> | T): T {
  if (payload && typeof payload === 'object') {
    const envelope = payload as ApiEnvelope<T>;
    const code = envelope.code === undefined ? undefined : String(envelope.code);
    const failedByCode = code !== undefined && !['0', '200', 'success'].includes(code.toLowerCase());
    const failedByFlag = envelope.success === false;

    if (failedByCode || failedByFlag) {
      throw new Error(friendlyApiMessage(envelope.message || envelope.msg || '接口请求失败'));
    }

    if (envelope.data !== undefined) {
      return envelope.data;
    }

    if (envelope.result !== undefined) {
      return envelope.result;
    }

    if (envelope.rows !== undefined) {
      return envelope.rows;
    }

    if (envelope.list !== undefined) {
      return envelope.list;
    }
  }

  return payload as T;
}

function removeStoredSession(portal?: AuthPortal) {
  const portals: AuthPortal[] = portal ? [portal] : ['admin', 'student'];

  portals.forEach((item) => {
    globalThis.localStorage?.removeItem(authStorageKeys[item].token);
    globalThis.localStorage?.removeItem(authStorageKeys[item].user);
  });

  if (!portal) {
    globalThis.localStorage?.removeItem(legacyAuthTokenKey);
    globalThis.localStorage?.removeItem(legacyAuthUserKey);
  }
}

function loginPathFor(portal?: AuthPortal): string {
  return portal === 'student' ? '/student/login' : '/admin/login';
}

function handleUnauthorized(portal?: AuthPortal) {
  removeStoredSession(portal);

  if (redirectingToLogin || typeof window === 'undefined') {
    return;
  }

  const loginPath = loginPathFor(portal);
  if (window.location.pathname === loginPath) {
    return;
  }

  redirectingToLogin = true;
  window.location.href = loginPath;
}

async function throwForResponse(response: Response, portal: AuthPortal | undefined, label: string): Promise<never> {
  if (response.status === 401) {
    handleUnauthorized(portal);
  }
  const message = friendlyApiMessage(await extractErrorMessage(response));
  throw new Error(message || `${label} 请求失败：${response.status}`);
}

async function extractErrorMessage(response: Response): Promise<string> {
  try {
    const text = await response.text();
    if (!text) {
      return '';
    }

    try {
      const payload = JSON.parse(text) as ApiEnvelope<unknown> & { error?: string };
      return String(payload.message || payload.msg || payload.error || text);
    } catch {
      return text;
    }
  } catch {
    return '';
  }
}

const apiMessageMap: Record<string, string> = {
  'Account is disabled': '账号已被禁用，请联系管理员',
  'Account does not belong to admin portal': '当前账号不是管理端账号',
  'Account does not belong to this portal': '当前账号不属于该登录入口',
  'Account number already exists': '账号已存在，请更换账号',
  'Assignment deadline has passed': '作业提交时间已截止',
  'Assignment is not open for answering': '作业暂未开放答题',
  'Assignment pass score must be between 0 and total score': '作业及格分必须在 0 到总分之间',
  'At least one camera is required': '请至少配置一路摄像头',
  'Auto rule question count must be greater than 0': '自动组卷题目数量必须大于 0',
  'Auto rule score must be greater than 0': '自动组卷每题分值必须大于 0',
  'Choice option key and text are required': '选项标识和选项内容不能为空',
  'Choice option keys cannot repeat': '选项标识不能重复',
  'Choice question must contain at least two options': '选择题至少需要两个选项',
  'Course academic year and semester are required': '请选择课程所属学年学期',
  'Course chapters cannot exceed 3 levels': '课程章节最多支持 3 级',
  'Course chapter title cannot exceed 20 characters': '课程章节标题不能超过 20 个字符',
  'Course chapter title is required': '请输入课程章节标题',
  'Course content title is required': '请输入课程内容标题',
  'Course content type is invalid': '课程内容类型无效',
  'Course cover is required': '请上传课程封面',
  'Course learning mode is invalid': '课程学习模式无效',
  'Course major is required': '请选择课程所属专业',
  'Course must contain content before publishing': '课程发布前请先添加课件或作业内容',
  'Course name cannot exceed 20 characters': '课程名称不能超过 20 个字符',
  'Course name is required': '请输入课程名称',
  'Course open end time must be after start time': '课程结束时间必须晚于开始时间',
  'Course teaching classes are required': '请选择授课班级',
  'Course teaching teachers are required': '请选择教学团队教师',
  'Courseware is not open for learning': '课件暂未开放学习',
  'Courseware learning end time must be after start time': '课件学习结束时间必须晚于开始时间',
  'Current password is incorrect': '原密码不正确',
  'File category is invalid': '文件分类无效',
  'File is required': '请选择要上传的文件',
  'ID card format is invalid': '身份证号格式不正确',
  'Import file must be an Excel file': '请上传 Excel 文件',
  'Import file name is required': '导入文件名称不能为空',
  'Import file size is required': '导入文件大小不能为空',
  'Import rows are required': '导入内容不能为空',
  'Invalid account or password': '账号或密码错误',
  'Invalid or expired token': '登录已过期，请重新登录',
  'Judgment answer must be TRUE or FALSE': '判断题答案请选择正确或错误',
  'Multiple choice must have at least two correct options': '多选题至少需要两个正确选项',
  'New password cannot equal current password': '新密码不能与原密码一致',
  'Not enough enabled questions for auto paper': '可用试题数量不足，无法自动组卷',
  'NVR host and channel cannot repeat in one classroom': '同一实训室内 NVR 地址和通道不能重复',
  'NVR port is invalid': 'NVR 端口无效',
  'Only submitted assignments can be reviewed': '只有已提交的作业才能批阅',
  'Paper contains disabled or missing questions': '试卷包含已停用或不存在的试题',
  'Paper must contain questions before publishing': '试卷发布前请先添加试题',
  'Paper question score must be greater than 0': '试题分值必须大于 0',
  'Paper questions cannot repeat': '试卷中的试题不能重复',
  'Password length must be 8-20 characters': '密码长度需为 8 到 20 位',
  'Password must contain letters and digits': '密码需同时包含字母和数字',
  'Phone format is invalid': '手机号格式不正确',
  'Question data is required': '请填写试题信息',
  'Question score must be greater than 0': '试题分值必须大于 0',
  'Question title is required': '请输入题干内容',
  'Question type is invalid': '试题题型无效',
  'Rejecting a resource requires a review comment': '驳回资源时请填写审核意见',
  'Resource already has a pending public application': '该资源已有待审核的公开申请',
  'Resource current version is already public': '该资源当前版本已公开',
  'Resource file cannot exceed 200MB': '资源文件不能超过 200MB',
  'Resource file type is not supported': '暂不支持该资源文件类型',
  'Review answer questions cannot repeat': '批阅题目不能重复',
  'Review answer score must be between 0 and question score': '单题得分必须在 0 到该题分值之间',
  'Reviewed score cannot exceed assignment total score': '批阅总分不能超过作业总分',
  'Score grade rules cannot overlap': '成绩等级规则不能重叠',
  'Score weights cannot be negative': '成绩权重不能为负数',
  'Score weights must add up to 100': '成绩权重总和必须为 100',
  'Selected resources are used by courses': '所选资源已被课程使用，不能删除',
  'Single choice must have exactly one correct option': '单选题必须且只能有一个正确选项',
  'Standard answer is required': '请输入参考答案',
  'Student already has an active room': '学员已有进行中的实训房间',
  'Team training roles must match team size': '小组角色数量必须与小组人数一致',
  'Training must have enabled students before publishing': '实训发布前请先配置可用学员',
  'Training name cannot exceed 128 characters': '实训名称不能超过 128 个字符',
  'Training open end time must be after start time': '实训结束时间必须晚于开始时间',
  'Training role has been claimed': '该实训角色已被认领',
  'Training role is claimed by another member': '该实训角色已被其他成员认领',
  'Training room is full': '实训房间人数已满'
};

function friendlyApiMessage(message?: string) {
  const raw = String(message || '').trim();
  if (!raw) {
    return '';
  }

  if (apiMessageMap[raw]) {
    return apiMessageMap[raw];
  }

  const matchedKey = Object.keys(apiMessageMap).find((key) => raw.includes(key));
  return matchedKey ? raw.replace(matchedKey, apiMessageMap[matchedKey]) : raw;
}

function requireStoredToken(path: string, options: ApiRequestOptions, portal?: AuthPortal): string {
  if (options.skipAuth) {
    return '';
  }

  const token = getStoredToken(portal);
  if (!token && portal) {
    handleUnauthorized(portal);
    throw new Error(`${options.fallbackLabel || path} 未登录`);
  }

  return token;
}

export async function requestJson<T>(path: string, options: ApiRequestOptions = {}): Promise<T> {
  const portal = options.authPortal || inferPortal(path);
  const token = requireStoredToken(path, options, portal);
  const headers = new Headers(options.headers);

  if (options.body && !(options.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const userId = options.skipAuth ? '' : getStoredUserId(portal);
  if (userId && !headers.has('X-User-Id')) {
    headers.set('X-User-Id', userId);
  }

  const response = await fetch(buildUrl(path), {
    credentials: 'include',
    ...options,
    headers
  });

  if (!response.ok) {
    await throwForResponse(response, portal, options.fallbackLabel || path);
  }

  const text = await response.text();
  if (!text) {
    return undefined as T;
  }

  return unwrapResponse<T>(JSON.parse(text));
}

export async function requestBlob(path: string, options: ApiRequestOptions = {}): Promise<FileDownload> {
  const portal = options.authPortal || inferPortal(path);
  const token = requireStoredToken(path, options, portal);
  const headers = new Headers(options.headers);

  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const userId = options.skipAuth ? '' : getStoredUserId(portal);
  if (userId && !headers.has('X-User-Id')) {
    headers.set('X-User-Id', userId);
  }

  const response = await fetch(buildUrl(path), {
    credentials: 'include',
    ...options,
    headers
  });

  if (!response.ok) {
    await throwForResponse(response, portal, options.fallbackLabel || path);
  }

  return {
    blob: await response.blob(),
    filename: filenameFromDisposition(response.headers.get('Content-Disposition'))
  };
}

export async function tryRequestJson<T>(paths: string[], options: ApiRequestOptions = {}): Promise<T> {
  const errors: string[] = [];

  for (const path of paths) {
    try {
      return await requestJson<T>(path, options);
    } catch (error) {
      errors.push(error instanceof Error ? error.message : String(error));
    }
  }

  throw new Error(errors[errors.length - 1] || '接口请求失败');
}

export function saveAuthSession(portal: AuthPortal, token: string, user?: unknown) {
  if (!token) {
    return;
  }

  globalThis.localStorage?.setItem(authStorageKeys[portal].token, token);
  if (user !== undefined) {
    globalThis.localStorage?.setItem(authStorageKeys[portal].user, JSON.stringify(user));
  }
}

export function clearAuthSession(portal?: AuthPortal) {
  removeStoredSession(portal);
}

function filenameFromDisposition(disposition: string | null): string | undefined {
  if (!disposition) {
    return undefined;
  }

  const utf8Match = /filename\*=UTF-8''([^;]+)/i.exec(disposition);
  if (utf8Match) {
    try {
      return decodeURIComponent(utf8Match[1].trim());
    } catch {
      return utf8Match[1].trim();
    }
  }

  const match = /filename="?([^";]+)"?/i.exec(disposition);
  return match ? match[1].trim() : undefined;
}
