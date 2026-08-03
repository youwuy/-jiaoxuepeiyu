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

export const mockResources: StudentResource[] = [];
