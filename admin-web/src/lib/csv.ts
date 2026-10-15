/** CSV an toàn: bọc ngoặc kép, chống formula injection (ô bắt đầu bằng = + - @ tab CR), thêm BOM để Excel đọc đúng tiếng Việt. */
export function csvCell(value: unknown): string {
  let s = value === null || value === undefined ? '' : String(value);
  if (/^[=+\-@\t\r]/.test(s)) s = "'" + s;
  return '"' + s.replace(/"/g, '""') + '"';
}

export function toCsv(headers: string[], rows: unknown[][]): string {
  return '﻿' + [headers, ...rows].map((r) => r.map(csvCell).join(',')).join('\r\n');
}

export function downloadCsv(filename: string, csv: string): void {
  const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }));
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
}

/** Tên file thể hiện loại dữ liệu và thời điểm xuất. */
export const exportFileName = (kind: string) => kind + '-' + new Date().toISOString().slice(0, 16).replace(/[:T-]/g, '') + '.csv';
