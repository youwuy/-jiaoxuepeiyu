export interface ApiRequestOptions extends RequestInit {
  fallbackLabel?: string;
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
const authTokenKey = 'jiaoxuepeiyu_token';
const authUserKey = 'jiaoxuepeiyu_user';

function buildUrl(path: string): string {
  if (/^https?:\/\//i.test(path)) {
    return path;
  }

  return `${apiBaseUrl}${path.startsWith('/') ? path : `/${path}`}`;
}

function getStoredToken(): string {
  return globalThis.localStorage?.getItem(authTokenKey) ?? '';
}

function getStoredUserId(): string {
  const storedUser = globalThis.localStorage?.getItem(authUserKey);

  if (!storedUser) {
    return '';
  }

  try {
    const user = JSON.parse(storedUser) as { id?: number | string; studentId?: number | string };
    return String(user.id ?? user.studentId ?? '');
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
      throw new Error(envelope.message || envelope.msg || '接口请求失败');
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

export async function requestJson<T>(path: string, options: ApiRequestOptions = {}): Promise<T> {
  const token = getStoredToken();
  const headers = new Headers(options.headers);

  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const userId = getStoredUserId();
  if (userId && !headers.has('X-User-Id')) {
    headers.set('X-User-Id', userId);
  }

  const response = await fetch(buildUrl(path), {
    credentials: 'include',
    ...options,
    headers
  });

  if (!response.ok) {
    throw new Error(`${options.fallbackLabel || path} 请求失败：${response.status}`);
  }

  const text = await response.text();
  if (!text) {
    return undefined as T;
  }

  return unwrapResponse<T>(JSON.parse(text));
}

export async function requestBlob(path: string, options: ApiRequestOptions = {}): Promise<FileDownload> {
  const token = getStoredToken();
  const headers = new Headers(options.headers);

  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const userId = getStoredUserId();
  if (userId && !headers.has('X-User-Id')) {
    headers.set('X-User-Id', userId);
  }

  const response = await fetch(buildUrl(path), {
    credentials: 'include',
    ...options,
    headers
  });

  if (!response.ok) {
    throw new Error(`${options.fallbackLabel || path} 请求失败：${response.status}`);
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

export function saveAuthSession(token: string, user?: unknown) {
  if (!token) {
    return;
  }

  globalThis.localStorage?.setItem(authTokenKey, token);
  if (user !== undefined) {
    globalThis.localStorage?.setItem(authUserKey, JSON.stringify(user));
  }
}

export function clearAuthSession() {
  globalThis.localStorage?.removeItem(authTokenKey);
  globalThis.localStorage?.removeItem(authUserKey);
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
