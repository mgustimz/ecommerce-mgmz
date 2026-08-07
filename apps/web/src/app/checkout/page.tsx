"use client";

import { createApiClient, type Address, type Cart, type ShippingRate } from "@mgmz/api-client";
import { formatCurrency } from "@mgmz/shared";
import { CustomerAuthGuard } from "@/components/customer-auth-guard";
import { getToken } from "@/lib/session";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function CheckoutPage() {
  return (
    <CustomerAuthGuard>
      <CheckoutContent />
    </CustomerAuthGuard>
  );
}

function CheckoutContent() {
  const router = useRouter();
  const [cart, setCart] = useState<Cart | null>(null);
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [selectedAddressId, setSelectedAddressId] = useState<number | null>(null);
  const [rates, setRates] = useState<ShippingRate[]>([]);
  const [selectedServiceCode, setSelectedServiceCode] = useState<string>("");
  const [ratesLoading, setRatesLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const defaultAddress = addresses.find((address) => address.defaultAddress) ?? addresses[0];

  async function loadRates(addressId: number) {
    const token = getToken();
    if (!token || !cart || cart.items.length === 0) return;

    setRatesLoading(true);
    try {
      const nextRates = await createApiClient().shipping.rates(token, { addressId });
      setRates(nextRates);
      // Preserve the current selection if still offered, otherwise pick the first.
      const stillOffered = nextRates.some((rate) => rate.serviceCode === selectedServiceCode);
      setSelectedServiceCode(stillOffered ? selectedServiceCode : (nextRates[0]?.serviceCode ?? ""));
    } catch (err) {
      setRates([]);
      setSelectedServiceCode("REG");
      setError(err instanceof Error ? err.message : "Failed to load shipping rates");
    } finally {
      setRatesLoading(false);
    }
  }

  useEffect(() => {
    const token = getToken();
    if (!token) {
      return;
    }
    Promise.all([createApiClient().cart.get(token), createApiClient().addresses.list(token)])
      .then(([nextCart, nextAddresses]) => {
        setCart(nextCart);
        setAddresses(nextAddresses);
        const first = nextAddresses.find((address) => address.defaultAddress) ?? nextAddresses[0];
        if (first) {
          setSelectedAddressId(first.id);
        }
      })
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load checkout"))
      .finally(() => setIsLoading(false));
  }, []);

  useEffect(() => {
    if (selectedAddressId !== null) {
      void loadRates(selectedAddressId);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedAddressId, cart?.items]);

  const selectedRate = rates.find((rate) => rate.serviceCode === selectedServiceCode);
  const shippingFee = selectedRate ? Number(selectedRate.fee) : 0;
  const subtotal = cart ? Number(cart.subtotal) : 0;
  const total = subtotal + shippingFee;

  async function checkout(formData: FormData) {
    const token = getToken();
    if (!token) return;
    setIsSubmitting(true);
    setError(null);
    try {
      const order = await createApiClient().orders.checkout(token, {
        addressId: Number(formData.get("addressId")),
        shippingServiceCode: selectedServiceCode || String(formData.get("shippingServiceCode")),
        paymentMethod: String(formData.get("paymentMethod")),
        notes: String(formData.get("notes") ?? "")
      });
      router.push(`/orders/${order.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Checkout failed");
    } finally {
      setIsSubmitting(false);
    }
  }

  if (isLoading) {
    return <div className="mx-auto max-w-6xl px-5 py-10 text-sm text-neutral-500">Loading checkout...</div>;
  }

  return (
    <div className="mx-auto max-w-6xl px-5 py-8">
      <nav className="mb-4 text-xs text-neutral-500">
        <Link href="/" className="hover:text-red-600">Home</Link> / <Link href="/cart" className="hover:text-red-600">Cart</Link> / <span>Checkout</span>
      </nav>
      <h1 className="text-3xl font-black">Checkout</h1>

      <div className="mt-6 grid gap-6 lg:grid-cols-[1fr_360px]">
        <form action={checkout} className="space-y-5 rounded-lg border border-neutral-200 bg-white p-6">
          {error && <p className="rounded border border-red-200 bg-red-50 p-3 text-sm font-bold text-red-700">{error}</p>}

          {addresses.length === 0 ? (
            <p className="rounded border border-amber-200 bg-amber-50 p-4 text-sm font-bold text-amber-800">
              Add a shipping address first.{" "}
              <Link href="/account/addresses" className="underline">Manage addresses</Link>
            </p>
          ) : (
            <div>
              <label className="text-sm font-bold text-neutral-700">Shipping address</label>
              <select
                name="addressId"
                value={selectedAddressId ?? ""}
                onChange={(event) => setSelectedAddressId(Number(event.target.value))}
                required
                className="input-field mt-2"
              >
                {addresses.map((address) => (
                  <option key={address.id} value={address.id}>
                    {address.label} - {address.street}, {address.city}
                  </option>
                ))}
              </select>
            </div>
          )}

          <div>
            <label className="text-sm font-bold text-neutral-700">Shipping service</label>
            <input type="hidden" name="shippingServiceCode" value={selectedServiceCode} />
            {ratesLoading ? (
              <p className="mt-3 text-sm text-neutral-500">Loading shipping rates...</p>
            ) : rates.length === 0 ? (
              <p className="mt-3 rounded border border-amber-200 bg-amber-50 p-3 text-xs font-bold text-amber-800">
                No shipping options available. Checkout will fall back to Regular shipping.
              </p>
            ) : (
              <div className="mt-3 space-y-2">
                {rates.map((rate) => {
                  const fee = Number(rate.fee);
                  const checked = rate.serviceCode === selectedServiceCode;
                  return (
                    <label
                      key={rate.serviceCode}
                      className={`flex cursor-pointer items-center justify-between gap-3 rounded-lg border p-3 transition ${
                        checked ? "border-red-600 bg-red-50" : "border-neutral-200 hover:border-neutral-300"
                      }`}
                    >
                      <span className="flex items-center gap-3">
                        <input
                          type="radio"
                          name="rate"
                          value={rate.serviceCode}
                          checked={checked}
                          onChange={() => setSelectedServiceCode(rate.serviceCode)}
                          className="accent-[#cc1d00]"
                        />
                        <span>
                          <span className="block font-bold">{rate.courierName} - {rate.serviceName}</span>
                          <span className="block text-xs text-neutral-500">{rate.estimatedDelivery}</span>
                        </span>
                      </span>
                      <span className="font-black" style={{ color: "#cc1d00" }}>
                        {fee === 0 ? <span className="rounded bg-emerald-100 px-2 py-0.5 text-xs font-black text-emerald-700">Free</span> : formatCurrency(fee)}
                      </span>
                    </label>
                  );
                })}
              </div>
            )}
          </div>

          <div>
            <label className="text-sm font-bold text-neutral-700">Payment method</label>
            <select name="paymentMethod" defaultValue="BANK_TRANSFER" className="input-field mt-2">
              <option value="BANK_TRANSFER">Bank Transfer</option>
              <option value="VIRTUAL_ACCOUNT">Virtual Account</option>
              <option value="EWALLET">E-Wallet</option>
              <option value="QRIS">QRIS</option>
              <option value="COD">Cash on Delivery</option>
            </select>
          </div>

          <div>
            <label className="text-sm font-bold text-neutral-700">Order notes (optional)</label>
            <textarea name="notes" rows={3} className="input-field mt-2" placeholder="Delivery instructions or notes for the seller"></textarea>
          </div>

          <button
            type="submit"
            disabled={!cart || cart.items.length === 0 || addresses.length === 0 || isSubmitting}
            className="btn-primary w-full disabled:cursor-not-allowed disabled:opacity-50"
          >
            {isSubmitting ? "Placing order..." : "Place order"}
          </button>
        </form>

        {cart && (
          <aside className="h-fit rounded-lg border border-neutral-200 bg-white p-6">
            <h2 className="text-base font-black">Order summary</h2>
            <ul className="mt-4 space-y-2 text-sm">
              {cart.items.map((item) => (
                <li key={item.id} className="flex justify-between">
                  <span className="truncate pr-2">{item.productName} x {item.quantity}</span>
                  <b>{formatCurrency(item.lineTotal)}</b>
                </li>
              ))}
            </ul>
            <div className="mt-4 border-t border-neutral-200 pt-4">
              <div className="flex justify-between text-sm"><span>Subtotal</span><b>{formatCurrency(cart.subtotal)}</b></div>
              <div className="mt-1 flex justify-between text-sm">
                <span>Shipping</span>
                <b>{ratesLoading ? "..." : formatCurrency(shippingFee)}</b>
              </div>
              <div className="mt-3 flex justify-between border-t border-neutral-200 pt-3 text-lg font-black">
                <span>Total</span>
                <b style={{ color: "#cc1d00" }}>{ratesLoading ? "..." : formatCurrency(total)}</b>
              </div>
            </div>
          </aside>
        )}
      </div>
    </div>
  );
}
