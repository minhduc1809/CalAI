import type { IconName } from '../components/ui/icons';

export type BadgeKey = 'pendingOrders' | 'pendingSubmissions' | 'dataRequests';

export interface NavItem { to: string; label: string; icon: IconName; badge?: BadgeKey; end?: boolean }
export interface NavGroup { id: string; label: string; items: NavItem[] }

/** Menu của web quản trị. Chỉ liệt kê màn đã hoạt động; màn chưa có thì không hiển thị. */
export const NAV_GROUPS: NavGroup[] = [
  { id: 'overview', label: 'Tổng quan', items: [{ to: '/', label: 'Dashboard', icon: 'Dashboard', end: true }] },
  {
    id: 'users',
    label: 'Người dùng & Hỗ trợ',
    items: [
      { to: '/users', label: 'Danh sách người dùng', icon: 'Users' },
      { to: '/data-requests', label: 'Yêu cầu dữ liệu', icon: 'DataRequests', badge: 'dataRequests' },
    ],
  },
  {
    id: 'billing',
    label: 'Doanh thu & Thanh toán',
    items: [
      { to: '/payments', label: 'Đơn chuyển khoản QR', icon: 'Payments', badge: 'pendingOrders' },
      { to: '/grants', label: 'Quyền Premium cấp tay', icon: 'Grants' },
      { to: '/revenue', label: 'Đối soát & doanh thu', icon: 'Revenue' },
    ],
  },
  {
    id: 'content',
    label: 'Nội dung',
    items: [
      { to: '/foods', label: 'Kho món', icon: 'FoodCatalog' },
      { to: '/submissions', label: 'Món người dùng gửi', icon: 'FoodReview', badge: 'pendingSubmissions' },
      { to: '/exercises', label: 'Kho bài tập', icon: 'Exercises' },
    ],
  },
  {
    id: 'ops',
    label: 'AI & Vận hành',
    items: [
      { to: '/ai', label: 'Theo dõi AI', icon: 'AIUsage' },
      { to: '/system-health', label: 'Sức khỏe hệ thống', icon: 'SystemHealth' },
    ],
  },
  { id: 'config', label: 'Cấu hình', items: [{ to: '/config', label: 'Tham số hệ thống', icon: 'Settings' }] },
  {
    id: 'security',
    label: 'Bảo mật & Quản trị',
    items: [
      { to: '/audit', label: 'Nhật ký quản trị', icon: 'AuditLog' },
      { to: '/security', label: 'Bảo mật tài khoản của tôi', icon: 'Security' },
    ],
  },
];

export const NAV_LOOKUP: NavItem[] = NAV_GROUPS.flatMap((g) => g.items);

/** Tên trang theo đường dẫn, dùng cho thanh trên cùng. */
export function titleFor(pathname: string): string {
  const hit = [...NAV_LOOKUP].sort((a, b) => b.to.length - a.to.length).find((n) => (n.to === '/' ? pathname === '/' : pathname.startsWith(n.to)));
  return hit?.label ?? 'NutriWise Admin';
}
