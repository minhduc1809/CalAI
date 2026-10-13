import type { IconName } from '../components/ui/icons';

export type BadgeKey = 'pendingOrders' | 'pendingSubmissions' | 'dataRequests';

export interface NavItem { to: string; label: string; icon: IconName; badge?: BadgeKey; end?: boolean }
export interface NavGroup { id: string; label: string; items: NavItem[] }

/** Menu của web quản trị. Chỉ liệt kê màn đã hoạt động; màn chưa có thì không hiển thị. */
export const NAV_GROUPS: NavGroup[] = [
  { id: 'overview', label: 'Bảng điều khiển', items: [{ to: '/', label: 'Tổng quan', icon: 'Dashboard', end: true }] },
  {
    id: 'users',
    label: 'Khách hàng',
    items: [
      { to: '/users', label: 'Người dùng', icon: 'Users' },
      { to: '/data-requests', label: 'Yêu cầu dữ liệu', icon: 'DataRequests', badge: 'dataRequests' },
    ],
  },
  {
    id: 'billing',
    label: 'Thanh toán',
    items: [
      { to: '/payments', label: 'Đơn thanh toán QR', icon: 'Payments', badge: 'pendingOrders' },
      { to: '/grants', label: 'Cấp Premium thủ công', icon: 'Grants' },
      { to: '/revenue', label: 'Báo cáo doanh thu', icon: 'Revenue' },
    ],
  },
  {
    id: 'content',
    label: 'Danh mục',
    items: [
      { to: '/foods', label: 'Danh mục món ăn', icon: 'FoodCatalog' },
      { to: '/submissions', label: 'Món người dùng đề xuất', icon: 'FoodReview', badge: 'pendingSubmissions' },
      { to: '/exercises', label: 'Danh mục bài tập', icon: 'Exercises' },
    ],
  },
  {
    id: 'ops',
    label: 'Vận hành',
    items: [
      { to: '/ai', label: 'Giám sát AI', icon: 'AIUsage' },
      { to: '/system-health', label: 'Tình trạng hệ thống', icon: 'SystemHealth' },
    ],
  },
  { id: 'config', label: 'Hệ thống', items: [{ to: '/config', label: 'Cấu hình hệ thống', icon: 'Settings' }] },
  {
    id: 'security',
    label: 'Bảo mật',
    items: [
      { to: '/audit', label: 'Nhật ký hoạt động', icon: 'AuditLog' },
      { to: '/security', label: 'Bảo mật tài khoản', icon: 'Security' },
    ],
  },
];

export const NAV_LOOKUP: NavItem[] = NAV_GROUPS.flatMap((g) => g.items);

/** Tên trang theo đường dẫn, dùng cho thanh trên cùng. */
export function titleFor(pathname: string): string {
  const hit = [...NAV_LOOKUP].sort((a, b) => b.to.length - a.to.length).find((n) => (n.to === '/' ? pathname === '/' : pathname.startsWith(n.to)));
  return hit?.label ?? 'NutriWise Admin';
}
