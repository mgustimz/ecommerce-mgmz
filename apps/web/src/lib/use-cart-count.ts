"use client";

import { createApiClient } from "@mgmz/api-client";
import { getAnonCartToken } from "@/lib/cart-cookie";
import { getToken } from "@/lib/session";
import { usePathname } from "next/navigation";
import { useCallback, useEffect, useState } from "react";
import { onCartUpdated } from "@/lib/cart-events";

export function useCartCount(): { count: number; isLoading: boolean } {
  const pathname = usePathname();
  const [count, setCount] = useState(0);
  const [isLoading, setIsLoading] = useState(true);

  const refresh = useCallback(() => {
    const api = createApiClient();
    const token = getToken();
    const load = token
      ? api.cart.get(token)
      : api.anonymousCart.get(getAnonCartToken());
    load
      .then((cart) => setCount(cart.items.reduce((sum, item) => sum + item.quantity, 0)))
      .catch(() => setCount(0))
      .finally(() => setIsLoading(false));
  }, []);

  useEffect(() => {
    setIsLoading(true);
    refresh();
  }, [refresh, pathname]);

  useEffect(() => onCartUpdated(refresh), [refresh]);

  return { count, isLoading };
}
