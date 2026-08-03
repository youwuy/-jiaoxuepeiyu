export interface PreviewableResource {
  resourceName?: string;
  resourceType?: string;
  fileName?: string;
  fileUrl?: string;
  previewUrl?: string;
}

export type ResourcePreviewKind = 'image' | 'video' | 'audio' | 'frame' | 'unsupported';

export function resourcePreviewSource(resource?: Pick<PreviewableResource, 'fileUrl' | 'previewUrl'> | null) {
  return resource?.previewUrl || resource?.fileUrl || '';
}

export function resourcePreviewKind(resource?: PreviewableResource | null): ResourcePreviewKind {
  const extension = fileExtension(resource);
  const rawType = String(resource?.resourceType || '');
  const type = rawType.toLowerCase();
  const enumType = rawType.toUpperCase();

  if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg'].includes(extension) || type.includes('图片')) {
    return 'image';
  }

  if (['mp4', 'webm', 'ogg', 'mov'].includes(extension) || type.includes('视频')) {
    return 'video';
  }

  if (['mp3', 'wav', 'm4a', 'aac', 'oga'].includes(extension) || type.includes('音频')) {
    return 'audio';
  }

  if (
    ['pdf', 'txt', 'html', 'htm'].includes(extension) ||
    enumType === 'DOCUMENT' ||
    type.includes('pdf') ||
    type.includes('文档') ||
    type.includes('文本')
  ) {
    return 'frame';
  }

  return 'unsupported';
}

function fileExtension(resource?: Pick<PreviewableResource, 'fileName' | 'fileUrl' | 'previewUrl'> | null) {
  const value = String(resource?.fileName || resource?.previewUrl || resource?.fileUrl || '').split('?')[0].split('#')[0];
  const match = value.match(/\.([a-z0-9]+)$/i);
  return match ? match[1].toLowerCase() : '';
}
