import { FormEvent, useCallback, useMemo, useRef, useState } from 'react';
import { ApiError } from '../services/api-client';

type Errors<T> = Partial<Record<keyof T, string>>;

interface Options<T> {
  initial: T;
  /** Kiểm tra phía client; trả về lỗi theo trường. Backend vẫn là nơi xác nhận cuối cùng. */
  validate?: (values: T) => Errors<T>;
  /** Gửi lên máy chủ. Ném lỗi để giữ form mở và hiện lỗi; trả về bình thường nghĩa là máy chủ đã xác nhận. */
  onSubmit: (values: T) => Promise<void>;
  onSuccess?: () => void;
}

/**
 * Vòng đời form thống nhất: giá trị mặc định → nhập → kiểm tra → gửi (khoá gửi lặp) → thành công (reset + onSuccess)
 * hoặc thất bại (giữ nguyên dữ liệu, hiện lỗi từ backend, không reset).
 */
export function useForm<T extends Record<string, unknown>>({ initial, validate, onSubmit, onSuccess }: Options<T>) {
  const [values, setValues] = useState<T>(initial);
  const [errors, setErrors] = useState<Errors<T>>({});
  const [formErrors, setFormErrors] = useState<string[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const inFlight = useRef(false);
  const baseline = useRef(initial);

  const setValue = useCallback(<K extends keyof T>(key: K, value: T[K]) => {
    setValues((v) => ({ ...v, [key]: value }));
    setErrors((e) => (e[key] ? { ...e, [key]: undefined } : e));
  }, []);

  const reset = useCallback((next?: T) => {
    baseline.current = next ?? initial;
    setValues(next ?? initial);
    setErrors({});
    setFormErrors([]);
  }, [initial]);

  const dirty = useMemo(() => JSON.stringify(values) !== JSON.stringify(baseline.current), [values]);

  const handleSubmit = useCallback(async (e?: FormEvent) => {
    e?.preventDefault();
    if (inFlight.current) return; // chặn gửi lặp khi bấm nhiều lần
    const found = validate ? validate(values) : {};
    const hasError = Object.values(found).some(Boolean);
    setErrors(found);
    if (hasError) {
      setFormErrors([]);
      return;
    }
    inFlight.current = true;
    setSubmitting(true);
    setFormErrors([]);
    try {
      await onSubmit(values);
      reset(values);
      onSuccess?.();
    } catch (err) {
      const messages = err instanceof ApiError ? (err.messages.length ? err.messages : [err.message]) : [(err as Error).message];
      // Gắn lỗi vào đúng trường khi thông báo bắt đầu bằng tên trường (kiểu class-validator); còn lại hiện ở phần tóm tắt
      const fieldErrors: Errors<T> = {};
      const rest: string[] = [];
      for (const m of messages) {
        const key = (Object.keys(values) as (keyof T)[]).find((k) => m.toLowerCase().startsWith(String(k).toLowerCase() + ' '));
        if (key && !fieldErrors[key]) fieldErrors[key] = m;
        else rest.push(m);
      }
      setErrors(fieldErrors);
      setFormErrors(rest);
    } finally {
      inFlight.current = false;
      setSubmitting(false);
    }
  }, [values, validate, onSubmit, onSuccess, reset]);

  return { values, setValue, errors, formErrors, submitting, dirty, handleSubmit, reset };
}
