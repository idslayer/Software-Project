
export const buildPageParams = (page: number, size: number, sorts: string[]) => {
  const p = new URLSearchParams();
  p.append('page', String(page));
  p.append('size', String(size));
  sorts.forEach(s => p.append('sort', s)); // sort=field,dir (repeat)
  return p;
};
