"use client";

import { createApiClient, type Coupon, type CouponInput } from "@mgmz/api-client";
import { AdminAuthGuard } from "@/components/admin-auth-guard";
import { forceAdminReLogin, getAdminToken } from "@/lib/session";
import Link from "next/link";
import { useEffect, useState } from "react";

export default function CouponsPage() {
  return (
    <AdminAuthGuard>
      <CouponsContent />
    </AdminAuthGuard>
  );
}

function CouponsContent() {
  const [coupons, setCoupons] = useState<Coupon[]>([]);
  const [editing, setEditing] = useState<Coupon | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function loadCoupons() {
    const token = getAdminToken();
    if (!token) {
      setError("Login as admin to manage coupons.");
      return;
    }
    setCoupons(await createApiClient().admin.coupons.list(token));
    setError(null);
  }

  useEffect(() => {
    loadCoupons().catch((err) => {
      if (err instanceof Error && /status 401/.test(err.message)) {
        forceAdminReLogin();
        return;
      }
      setError(err instanceof Error ? err.message : "Failed to load coupons");
    });
  }, []);

  async function saveCoupon(formData: FormData) {
    const token = getAdminToken();
    if (!token) return;

    const validUntil = String(formData.get("validUntil") || "");
    const body: CouponInput = {
      code: String(formData.get("code")),
      discountType: String(formData.get("discountType")) as CouponInput["discountType"],
      discountValue: String(formData.get("discountValue")),
      minSubtotal: String(formData.get("minSubtotal") || "0"),
      validFrom: new Date().toISOString(),
      validUntil: validUntil ? new Date(validUntil).toISOString() : null,
      maxRedemptions: formData.get("maxRedemptions") ? Number(formData.get("maxRedemptions")) : null,
      active: formData.get("active") === "on"
    };

    try {
      if (editing) {
        await createApiClient().admin.coupons.update(token, editing.id, body);
      } else {
        await createApiClient().admin.coupons.create(token, body);
      }
      setEditing(null);
      await loadCoupons();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to save coupon");
    }
  }

  async function deleteCoupon(id: number) {
    const token = getAdminToken();
    if (!token || !confirm("Delete this coupon?")) return;
    try {
      await createApiClient().admin.coupons.delete(token, id);
      await loadCoupons();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete coupon");
    }
  }

  function couponLabel(coupon: Coupon) {
    return coupon.discountType === "PERCENT" ? `${coupon.discountValue}% off` : `Rp ${coupon.discountValue} off`;
  }

  return (
    <div className="grid gap-8 lg:grid-cols-[1fr_380px]">
      <section>
        <div className="flex items-center justify-between gap-4">
          <h1 className="text-4xl font-black">Coupons</h1>
        </div>
        {error && <p className="mt-5 rounded-2xl bg-red-950 p-4 font-bold text-red-200">{error}</p>}
        <Link href="/dashboard" className="mt-3 inline-block text-xs font-bold text-cyan-300">Back to dashboard</Link>
        <div className="mt-6 overflow-hidden rounded-3xl border border-slate-800 bg-slate-950">
          {coupons.map((coupon) => (
            <div key={coupon.id} className="grid gap-3 border-b border-slate-800 p-4 md:grid-cols-[1fr_150px_120px_auto] md:items-center">
              <div>
                <p className="font-black uppercase text-white">{coupon.code}</p>
                <p className="text-xs text-slate-400">
                  {couponLabel(coupon)} · min {formatCurrency(coupon.minSubtotal)} · {coupon.redemptions}{coupon.maxRedemptions ? `/${coupon.maxRedemptions}` : ""} used
                  {coupon.active ? "" : " · inactive"}
                </p>
              </div>
              <p className="text-sm text-slate-300">{coupon.discountType === "PERCENT" ? "Percent" : "Fixed"}</p>
              <Link href="#" className="hidden" aria-hidden>spacer</Link>
              <div className="flex gap-3 text-sm font-bold">
                <button onClick={() => setEditing(coupon)} className="text-cyan-300">Edit</button>
                <button onClick={() => void deleteCoupon(coupon.id)} className="text-red-300">Delete</button>
              </div>
            </div>
          ))}
          {coupons.length === 0 && !error && <p className="p-6 text-slate-400">No coupons yet.</p>}
        </div>
      </section>

      <form key={editing?.id ?? "new"} action={saveCoupon} className="h-fit rounded-3xl border border-slate-800 bg-slate-950 p-6">
        <h2 className="text-2xl font-black">{editing ? "Edit coupon" : "New coupon"}</h2>
        <div className="mt-5 grid gap-3">
          <label className="block text-sm font-bold text-slate-300">
            Code
            <input name="code" required defaultValue={editing?.code} className="mt-2 w-full rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-white uppercase" />
          </label>
          <label className="block text-sm font-bold text-slate-300">
            Discount type
            <select name="discountType" defaultValue={editing?.discountType ?? "PERCENT"} className="mt-2 w-full rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-white">
              <option value="PERCENT">Percent (%)</option>
              <option value="FIXED">Fixed (Rp)</option>
            </select>
          </label>
          <label className="block text-sm font-bold text-slate-300">
            Discount value
            <input name="discountValue" type="number" step="0.01" min="0.01" required defaultValue={editing?.discountValue} className="mt-2 w-full rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-white" />
          </label>
          <label className="block text-sm font-bold text-slate-300">
            Minimum subtotal (Rp)
            <input name="minSubtotal" type="number" step="0.01" min="0" defaultValue={editing?.minSubtotal ?? 0} className="mt-2 w-full rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-white" />
          </label>
          <label className="block text-sm font-bold text-slate-300">
            Valid until (optional)
            <input name="validUntil" type="datetime-local" defaultValue={toDatetimeLocal(editing?.validUntil)} className="mt-2 w-full rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-white" />
          </label>
          <label className="block text-sm font-bold text-slate-300">
            Max redemptions (optional)
            <input name="maxRedemptions" type="number" min="1" defaultValue={editing?.maxRedemptions ?? ""} className="mt-2 w-full rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-white" />
          </label>
          <label className="flex items-center gap-2 text-sm font-bold text-slate-300">
            <input name="active" type="checkbox" defaultChecked={editing ? editing.active : true} /> Active
          </label>
        </div>
        <div className="mt-5 flex gap-3">
          <button className="rounded-2xl bg-cyan-400 px-5 py-3 font-black text-slate-950">Save</button>
          {editing && (
            <button type="button" onClick={() => setEditing(null)} className="rounded-2xl border border-slate-700 px-5 py-3 font-black text-white">
              Cancel
            </button>
          )}
        </div>
      </form>
    </div>
  );
}

function formatCurrency(value: string | number) {
  return new Intl.NumberFormat("id-ID", { style: "currency", currency: "IDR", maximumFractionDigits: 0 }).format(Number(value));
}

function toDatetimeLocal(iso: string | null | undefined) {
  if (!iso) return "";
  return new Date(iso).toISOString().slice(0, 16);
}
