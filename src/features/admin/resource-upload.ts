export const RESOURCE_EXTENSIONS = [
  'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'jpg', 'jpeg', 'png', 'gif',
  'mp4', 'mov', 'avi', 'flv', 'wmv', 'mp3', 'wav', 'wma', 'pdf'
];

const COVER_EXTENSIONS = ['jpg', 'jpeg', 'png'];
const MAX_RESOURCE_SIZE = 200 * 1024 * 1024;
const MAX_COVER_SIZE = 5 * 1024 * 1024;

export function validateResourceUpload(file: File, kind: 'resource' | 'cover' = 'resource'): string | undefined {
  const extension = file.name.split('.').pop()?.toLowerCase() || '';
  if (kind === 'cover') {
    if (!COVER_EXTENSIONS.includes(extension)) return '封面图仅支持 JPG、PNG 格式';
    if (file.size > MAX_COVER_SIZE) return '封面图大小不能超过 5MB';
    return undefined;
  }
  if (!RESOURCE_EXTENSIONS.includes(extension)) return '暂不支持该资源文件类型';
  if (file.size > MAX_RESOURCE_SIZE) return '资源文件不能超过 200MB';
  return undefined;
}

export function fileFromDrop(event: DragEvent): File | null {
  return event.dataTransfer?.files?.[0] || null;
}

export function formatUploadSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
  return `${(bytes / 1024 / 1024 / 1024).toFixed(1)} GB`;
}
