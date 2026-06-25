import type { AuthSession } from "@mgmz/shared";

const SESSION_KEY = "mgmz_admin_session";

export function getAdminSession(): AuthSession | null {
  if (typeof window === "undefined") {
    return null;
  }

  const raw = window.localStorage.getItem(SESSION_KEY);
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as AuthSession;
  } catch {
    window.localStorage.removeItem(SESSION_KEY);
    return null;
  }
}

export function setAdminSession(session: AuthSession) {
  window.localStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

export function clearAdminSession() {
  window.localStorage.removeItem(SESSION_KEY);
}

export function getAdminToken() {
  const session = getAdminSession();
  return session?.role === "ADMIN" ? session.token : null;
}
