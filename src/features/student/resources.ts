export interface StudentResource {
  id: number;
  title: string;
  category: string;
  type: string;
  courseName?: string;
  author?: string;
  coverUrl?: string;
  previewUrl?: string;
  fileUrl?: string;
  updatedAt: string;
  size: string;
}

export interface ResourceFilters {
  keyword?: string;
  category?: string;
  type?: string;
}

export function filterResources<T extends Pick<StudentResource, 'title' | 'category' | 'type'>>(
  resources: T[],
  filters: ResourceFilters
): T[] {
  const keyword = filters.keyword?.trim().toLowerCase() ?? '';
  const category = filters.category ?? '全部';
  const type = filters.type ?? '全部';

  return resources.filter((resource) => {
    const matchesKeyword = !keyword || resource.title.toLowerCase().includes(keyword);
    const matchesCategory = category === '全部' || resource.category === category;
    const matchesType = type === '全部' || resource.type === type;
    return matchesKeyword && matchesCategory && matchesType;
  });
}

const coverUrls = {
  course: new URL('../../assets/resource-cover-course.jpg', import.meta.url).href,
  signalVideo: new URL('../../assets/resource-cover-signal-video.jpg', import.meta.url).href,
  train: new URL('../../assets/resource-cover-train.jpg', import.meta.url).href,
  manual: new URL('../../assets/resource-cover-manual.jpg', import.meta.url).href,
  power: new URL('../../assets/resource-cover-power.jpg', import.meta.url).href,
  yard: new URL('../../assets/resource-cover-yard.jpg', import.meta.url).href,
  chain: new URL('../../assets/resource-cover-chain.jpg', import.meta.url).href,
  ats: new URL('../../assets/resource-cover-ats.jpg', import.meta.url).href
};

export function coverForResourceType(type: string): string {
  if (type === '视频') {
    return coverUrls.signalVideo;
  }

  if (type === '图像' || type === '图片') {
    return coverUrls.train;
  }

  if (type === '音频') {
    return coverUrls.power;
  }

  if (type === '文本文档' || type === 'PDF') {
    return coverUrls.manual;
  }

  return coverUrls.course;
}

export const mockResources: StudentResource[] = [
  {
    id: 4001,
    title: '城市轨道交通概论-教学课件',
    category: '城市轨道交通运营管理',
    type: '演示文稿',
    author: '王老师',
    coverUrl: coverUrls.course,
    updatedAt: '2025-02-15',
    size: '18.6 MB'
  },
  {
    id: 4002,
    title: 'CBTC信号系统原理讲解',
    category: '城市轨道交通通信信号技术',
    type: '视频',
    author: '李老师',
    coverUrl: coverUrls.signalVideo,
    updatedAt: '2025-02-20',
    size: '246 MB'
  },
  {
    id: 4003,
    title: '城轨车辆构造高清图集',
    category: '城市轨道交通通信信号技术',
    type: '图像',
    author: '赵老师',
    coverUrl: coverUrls.train,
    updatedAt: '2025-03-01',
    size: '32.4 MB'
  },
  {
    id: 4004,
    title: '车站运营管理标准手册',
    category: '城市轨道交通通信信号技术',
    type: '文本文档',
    author: '王老师',
    coverUrl: coverUrls.manual,
    updatedAt: '2025-01-10',
    size: '9.2 MB'
  },
  {
    id: 4005,
    title: '供电系统故障案例分析',
    category: '城市轨道交通通信信号技术',
    type: '音频',
    author: '陈老师',
    coverUrl: coverUrls.power,
    updatedAt: '2025-02-28',
    size: '86 MB'
  },
  {
    id: 4006,
    title: '车站值班员实训试题库',
    category: '城市轨道交通通信信号技术',
    type: '实训试题',
    author: '刘老师',
    coverUrl: coverUrls.yard,
    updatedAt: '2025-03-05',
    size: '12.5 MB'
  },
  {
    id: 4007,
    title: '城轨安全操作规范视频',
    category: '城市轨道交通通信信号技术',
    type: '视频',
    author: '刘老师',
    coverUrl: coverUrls.chain,
    updatedAt: '2025-03-08',
    size: '316 MB'
  },
  {
    id: 4008,
    title: '联锁系统与ATS教学课件',
    category: '城市轨道交通通信信号技术',
    type: '演示文稿',
    author: '李老师',
    coverUrl: coverUrls.ats,
    updatedAt: '2025-02-18',
    size: '22.1 MB'
  }
];
