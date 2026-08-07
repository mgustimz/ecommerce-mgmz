const CART_UPDATED_EVENT = "cart:updated";

export function dispatchCartUpdated() {
  if (typeof window === "undefined") return;
  window.dispatchEvent(new CustomEvent(CART_UPDATED_EVENT));
}

export function onCartUpdated(handler: () => void): () => void {
  if (typeof window === "undefined") return () => {};
  window.addEventListener(CART_UPDATED_EVENT, handler);
  return () => window.removeEventListener(CART_UPDATED_EVENT, handler);
}
