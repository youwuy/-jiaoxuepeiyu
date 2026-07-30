export interface StudentResource {
  id: number;
  title: string;
  category: string;
  type: string;
  courseName?: string;
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

export const mockResources: StudentResource[] = [
  {
    id: 4001,
    title: '城轨概论课件',
    category: '课程资源',
    type: 'PPT',
    courseName: '城市轨道交通概论',
    updatedAt: '2026-07-28',
    size: '18.6 MB'
  },
  {
    id: 4002,
    title: '车站布局视频',
    category: '实训资源',
    type: '视频',
    courseName: '城市轨道交通概论',
    updatedAt: '2026-07-24',
    size: '246 MB'
  },
  {
    id: 4003,
    title: '信号系统图册',
    category: '课程资源',
    type: '图片',
    courseName: '轨道交通信号系统',
    updatedAt: '2026-07-21',
    size: '32.4 MB'
  },
  {
    id: 4004,
    title: '实训操作规范手册',
    category: '公开资料',
    type: 'PDF',
    updatedAt: '2026-07-16',
    size: '9.2 MB'
  }
];
