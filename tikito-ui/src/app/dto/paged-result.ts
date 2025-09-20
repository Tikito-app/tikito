export class PagedResult<T> {
  items: T[];
  currentPage: number;
  totalItems: number;
}
