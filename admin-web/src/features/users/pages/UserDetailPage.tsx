import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { PageHeader, StatusBadge } from '../../../components/data-display/parts';
import { ReasonPasswordDialog } from '../../../components/feedback/ConfirmDialogs';
import { Alert, EmptyState, ErrorState, LoadingState } from '../../../components/feedback/States';
import { Button } from '../../../components/ui/Button';
import { useToast } from '../../../components/ui/Toast';
import { fmtDate, fmtDateTime, fmtNumber, fmtVnd } from '../../../lib/format';
import { GrantDialog } from '../../premium-grants/components/GrantDialog';
import { OrderStatusBadge } from '../../qr-orders/components/OrderStatusBadge';
import type { OrderStatus } from '../../qr-orders/types/order.types';
import { userApi } from '../api/user.api';
import { AccountStatusBadge } from '../components/UserBadges';

type Action = 'lock' | 'unlock' | 'revoke' | null;

const USAGE_LABELS: [keyof NonNullable<ReturnType<typeof useDetail>['data']>['usageToday'], string][] = [
  ['aiPhoto', 'Nhận diện ảnh'],
  ['chatMessages', 'Tin nhắn chat'],
  ['chatTokens', 'Token chat'],
  ['menuScans', 'Quét thực đơn'],
  ['suggestMeals', 'Gợi ý món AI'],
  ['barcodeLookups', 'Tra mã vạch'],
];

function useDetail(id: string) {
  return useQuery({ queryKey: ['user', id], queryFn: ({ signal }) => userApi.detail(id, signal) });
}

export default function UserDetailPage() {
  const { id = '' } = useParams();
  const toast = useToast();
  const qc = useQueryClient();
  const detail = useDetail(id);
  const billing = useQuery({ queryKey: ['user-billing', id], queryFn: ({ signal }) => userApi.billing(id, signal) });
  const [action, setAction] = useState<Action>(null);
  const [granting, setGranting] = useState(false);

  const run = useMutation({
    mutationFn: ({ kind, reason, password }: { kind: Exclude<Action, null>; reason: string; password: string }) =>
      kind === 'lock' ? userApi.lock(id, { reason, password }) : kind === 'unlock' ? userApi.unlock(id, { reason, password }) : userApi.revokeSessions(id, { reason, password }),
    onSuccess: (_d, v) => {
      qc.invalidateQueries({ queryKey: ['user', id] });
      qc.invalidateQueries({ queryKey: ['users'] });
      toast.success(v.kind === 'lock' ? 'Đã khoá tài khoản' : v.kind === 'unlock' ? 'Đã mở khoá tài khoản' : 'Đã đăng xuất mọi thiết bị');
    },
  });

  const crumbs = [{ label: 'Người dùng & Hỗ trợ' }, { label: 'Danh sách người dùng', to: '/users' }, { label: 'Chi tiết' }];
  if (detail.isLoading) return (<><PageHeader title="Chi tiết người dùng" breadcrumbs={crumbs} /><LoadingState lines={8} /></>);
  if (detail.isError || !detail.data) return (<><PageHeader title="Chi tiết người dùng" breadcrumbs={crumbs} /><ErrorState message={(detail.error as Error)?.message ?? 'Không tải được'} onRetry={() => detail.refetch()} /></>);

  const u = detail.data;
  const isUser = u.role === 'USER';
  const b = billing.data;

  return (
    <>
      <PageHeader
        title={u.email ?? u.username}
        description={u.name ?? undefined}
        breadcrumbs={crumbs}
        actions={isUser && (
          <>
            <Button icon="Grants" onClick={() => setGranting(true)}>Cấp Premium</Button>
            <Button icon="Logout" onClick={() => setAction('revoke')}>Đăng xuất mọi thiết bị</Button>
            {u.isActive ? <Button variant="danger" icon="Lock" onClick={() => setAction('lock')}>Khoá tài khoản</Button> : <Button variant="primary" icon="Unlock" onClick={() => setAction('unlock')}>Mở khoá</Button>}
          </>
        )}
      />
      {!isUser && <Alert kind="info">Đây là tài khoản quản trị. Không thể khoá hoặc đăng xuất từ màn này.</Alert>}
      <div className="alert info" style={{ margin: '12px 0' }}>Màn này chỉ hiển thị thông tin tài khoản, gói và giao dịch. Bữa ăn, cân nặng, chat, ảnh và hồ sơ cơ thể của người dùng không có trong hệ thống quản trị.</div>

      <div className="grid cols-2" style={{ marginBottom: 16 }}>
        <div className="card">
          <div className="card-title">Tài khoản và xác thực</div>
          <dl className="kv">
            <dt>Trạng thái</dt><dd><AccountStatusBadge active={u.isActive} /></dd>
            <dt>Email</dt><dd>{u.email ?? '—'} <StatusBadge tone={u.isEmailVerified ? 'success' : 'warning'}>{u.isEmailVerified ? 'Đã xác thực' : 'Chưa xác thực'}</StatusBadge></dd>
            <dt>Tên đăng nhập</dt><dd>{u.username}</dd>
            <dt>Đăng nhập bằng</dt><dd>{u.authProvider === 'GOOGLE' ? 'Google' : 'Mật khẩu'}</dd>
            <dt>Ngày đăng ký</dt><dd>{fmtDateTime(u.createdAt)}</dd>
            <dt>Múi giờ</dt><dd>{u.timezone}</dd>
            <dt>Đồng ý chính sách</dt><dd>{u.privacyAcceptedVersion ? `Phiên bản ${u.privacyAcceptedVersion} · ${fmtDateTime(u.privacyAcceptedAt)}` : <span className="muted">Chưa ghi nhận</span>}</dd>
          </dl>
        </div>

        <div className="card">
          <div className="card-title">Gói và quyền lợi</div>
          <dl className="kv">
            <dt>Gói hiện tại</dt><dd><StatusBadge tone={u.plan === 'PREMIUM' ? 'success' : 'neutral'}>{u.plan === 'PREMIUM' ? 'Premium' : 'Free'}</StatusBadge></dd>
            {u.subscriptionState && (<>
              <dt>Đăng ký (Google Play)</dt><dd>{u.subscriptionState.productId} · {u.subscriptionState.status}{u.subscriptionState.isTrial ? ' · dùng thử' : ''}<div className="muted">Hết hạn {fmtDate(u.subscriptionState.expiryTime)} · {u.subscriptionState.autoRenewing ? 'tự gia hạn' : 'không tự gia hạn'}</div></dd>
            </>)}
            <dt>Quyền cấp còn hiệu lực</dt>
            <dd>{u.manualGrants.length === 0 ? <span className="muted">Không có</span> : u.manualGrants.map((g) => (
              <div key={g.id}>{g.source === 'QR_ORDER' ? 'Từ đơn QR' : 'Admin cấp'} · đến {fmtDate(g.endsAt)}<div className="muted">{g.reason}</div></div>
            ))}</dd>
          </dl>
        </div>
      </div>

      <div className="grid cols-2" style={{ marginBottom: 16 }}>
        <div className="card">
          <div className="card-title">Hạn mức đã dùng hôm nay (số đếm)</div>
          <dl className="kv">
            {USAGE_LABELS.map(([k, label]) => (<><dt key={`${k}-l`}>{label}</dt><dd key={`${k}-v`}>{fmtNumber(u.usageToday[k])}</dd></>))}
          </dl>
        </div>
        <div className="card">
          <div className="card-title">Thiết bị đang đăng nhập ({u.refreshSessions.length})</div>
          {u.refreshSessions.length === 0 ? <span className="muted">Không có phiên nào đang hoạt động.</span> : (
            <table className="table">
              <thead><tr><th>Thiết bị</th><th>Đăng nhập</th><th>Dùng gần nhất</th></tr></thead>
              <tbody>{u.refreshSessions.map((s) => (<tr key={s.id}><td>{s.deviceName ?? 'Không rõ'}</td><td>{fmtDateTime(s.createdAt)}</td><td>{fmtDateTime(s.lastUsedAt)}</td></tr>))}</tbody>
            </table>
          )}
        </div>
      </div>

      <div className="card">
        <div className="row between"><div className="card-title">Giao dịch</div><Link to={`/payments?search=${encodeURIComponent(u.email ?? '')}`}>Mở trong đơn QR</Link></div>
        {billing.isLoading ? <LoadingState lines={3} /> : billing.isError ? <ErrorState message={(billing.error as Error).message} onRetry={() => billing.refetch()} /> : !b || (b.paymentOrders.length === 0 && b.purchases.length === 0) ? (
          <EmptyState title="Chưa có giao dịch" icon="Payments" />
        ) : (
          <div className="table-wrap">
            <table className="table">
              <thead><tr><th>Mã / Đơn</th><th>Gói</th><th>Số tiền</th><th>Trạng thái</th><th>Thời gian</th></tr></thead>
              <tbody>
                {b.paymentOrders.map((o) => (<tr key={o.id}><td><b>{o.code}</b></td><td>{o.productId}</td><td>{fmtVnd(o.amount)}</td><td><OrderStatusBadge status={o.status as OrderStatus} /></td><td>{fmtDateTime(o.paidAt ?? o.createdAt)}</td></tr>))}
                {b.purchases.map((p) => (<tr key={p.id}><td>Google Play {p.orderId ?? ''}</td><td>{p.productId}</td><td>—</td><td><StatusBadge tone="success">Đã xác minh</StatusBadge></td><td>{fmtDateTime(p.purchaseTime)}</td></tr>))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {granting && <GrantDialog fixedUser={{ id: u.id, email: u.email }} onClose={() => setGranting(false)} />}
      {action && (
        <ReasonPasswordDialog
          title={action === 'lock' ? 'Khoá tài khoản' : action === 'unlock' ? 'Mở khoá tài khoản' : 'Đăng xuất mọi thiết bị'}
          subject={<b>{u.email ?? u.username}</b>}
          description={action === 'lock' ? 'Người dùng bị đăng xuất khỏi mọi thiết bị và không đăng nhập được cho tới khi mở khoá.' : action === 'unlock' ? 'Người dùng đăng nhập lại được bình thường.' : 'Mọi phiên đăng nhập hiện có bị thu hồi. Người dùng vẫn đăng nhập lại được.'}
          confirmLabel={action === 'lock' ? 'Khoá tài khoản' : action === 'unlock' ? 'Mở khoá' : 'Đăng xuất thiết bị'}
          danger={action !== 'unlock'}
          onConfirm={async (v) => { await run.mutateAsync({ kind: action, ...v }); }}
          onClose={() => setAction(null)}
        />
      )}
    </>
  );
}
