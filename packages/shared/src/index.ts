export type UserRole = "CUSTOMER" | "ADMIN";

export type AuthSession = {
  token: string;
  userId: number;
  name: string;
  email: string;
  role: UserRole;
};

export const currencyFormatter = new Intl.NumberFormat("id-ID", {
  style: "currency",
  currency: "IDR",
  maximumFractionDigits: 0
});

export function formatCurrency(value: number | string) {
  return currencyFormatter.format(Number(value));
}
