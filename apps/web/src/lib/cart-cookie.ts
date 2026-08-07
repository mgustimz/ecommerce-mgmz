const COOKIE_NAME = "mgmz_anon_cart_client";
const MAX_AGE_SECONDS = 604800; // 7 days

function readCookie(name: string): string | null {
  if (typeof window === "undefined") return null;
  const match = document.cookie.match(new RegExp("(^| )" + name + "=([^;]+)"));
  return match ? decodeURIComponent(match[2]) : null;
}

function writeCookie(name: string, value: string, maxAgeSeconds: number) {
  if (typeof window === "undefined") return;
  document.cookie = `${name}=${encodeURIComponent(value)}; Path=/; Max-Age=${maxAgeSeconds}; SameSite=Lax`;
}

function generateToken(): string {
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, (char) => {
    const random = (Math.random() * 16) | 0;
    const value = char === "x" ? random : (random & 0x3) | 0x8;
    return value.toString(16);
  });
}

export function getAnonCartToken(): string {
  const existing = readCookie(COOKIE_NAME);
  if (existing) {
    return existing;
  }
  const token = generateToken();
  writeCookie(COOKIE_NAME, token, MAX_AGE_SECONDS);
  return token;
}

export function clearAnonCartToken() {
  writeCookie(COOKIE_NAME, "", 0);
}
