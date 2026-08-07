"use client";

import { createApiClient, type InventoryMovement, type InventoryMovementType } from "@mgmz/api-client";
import { AdminAuthGuard } from "@/components/admin-auth-guard";
import { forceAdminReLogin, getAdminToken } from "@/lib/session";
import { useEffect, useMemo, useState } from "react";

const TYPE_LABELS: Record<InventoryMovementType, string> = {
  PRODUCT_CREATED: "Product created",
  ADMIN_ADJUSTMENT: "Admin adjustment",
  ORDER_CREATED: "Order placed",
  ORDER_CANCELLED: "Order cancelled"
};

const TYPE_STYLES: Record<InventoryMovementType, string> = {
  PRODUCT_CREATED: "bg-emerald-500/15 text-emerald-300",
  ADMIN_ADJUSTMENT: "bg-cyan-400/15 text-cyan-300",
  ORDER_CREATED: "bg-amber-400/15 text-amber-300",
  ORDER_CANCELLED: "bg-rose-500/15 text-rose-300"
};

export default function InventoryPage() {
  return (
    <AdminAuthGuard>
      <InventoryContent />
    </AdminAuthGuard>
  );
}

function InventoryContent() {
  const [movements, setMovements] = useState<InventoryMovement[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [productFilter, setProductFilter] = useState("");
  const [typeFilter, setTypeFilter] = useState<InventoryMovementType | "ALL">("ALL");

  async function loadMovements(productId?: number) {
    const token = getAdminToken();
    if (!token) {
      setError("Login as admin to view inventory.");
      setIsLoading(false);
      return;
    }
    try {
      setMovements(await createApiClient().admin.inventory.list(token, { productId }));
      setError(null);
    } catch (err) {
      if (err instanceof Error && /status 401/.test(err.message)) {
        forceAdminReLogin();
        return;
      }
      setError(err instanceof Error ? err.message : "Failed to load inventory");
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void loadMovements();
  }, []);

  function applyFilter() {
    const productId = productFilter ? Number(productFilter) : undefined;
    if (productId !== undefined && Number.isNaN(productId)) {
      setError("Product ID must be a number");
      return;
    }
    setError(null);
    setIsLoading(true);
    void loadMovements(productId);
  }

  function clearFilter() {
    setProductFilter("");
    setIsLoading(true);
    void loadMovements();
  }

  const filtered = useMemo(() => {
    if (typeFilter === "ALL") return movements;
    return movements.filter((movement) => movement.type === typeFilter);
  }, [movements, typeFilter]);

  const totals = useMemo(() => {
    const total = movements.length;
    const positive = movements.filter((m) => m.quantityChange > 0).length;
    const negative = movements.filter((m) => m.quantityChange < 0).length;
    return { total, positive, negative };
  }, [movements]);

  return (
    <div>
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-4xl font-black">Inventory</h1>
          <p className="mt-1 text-sm text-slate-400">Track stock movements across products and orders.</p>
        </div>
        <div className="grid grid-cols-3 gap-3 text-center">
          <div className="rounded-2xl border border-slate-800 bg-slate-950 px-5 py-3">
            <p className="text-[10px] font-black uppercase tracking-widest text-slate-400">Movements</p>
            <p className="mt-1 text-xl font-black text-white">{totals.total}</p>
          </div>
          <div className="rounded-2xl border border-slate-800 bg-slate-950 px-5 py-3">
            <p className="text-[10px] font-black uppercase tracking-widest text-emerald-300">Stock in</p>
            <p className="mt-1 text-xl font-black text-emerald-300">{totals.positive}</p>
          </div>
          <div className="rounded-2xl border border-slate-800 bg-slate-950 px-5 py-3">
            <p className="text-[10px] font-black uppercase tracking-widest text-rose-300">Stock out</p>
            <p className="mt-1 text-xl font-black text-rose-300">{totals.negative}</p>
          </div>
        </div>
      </div>

      <div className="mt-6 grid gap-3 md:grid-cols-[1fr_200px_160px_auto_auto]">
        <input
          type="number"
          value={productFilter}
          onChange={(event) => setProductFilter(event.target.value)}
          placeholder="Filter by product ID"
          className="rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-white placeholder:text-slate-500 focus:border-cyan-400 focus:outline-none"
        />
        <select
          value={typeFilter}
          onChange={(event) => setTypeFilter(event.target.value as InventoryMovementType | "ALL")}
          className="rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-white focus:border-cyan-400 focus:outline-none"
        >
          <option value="ALL">All types</option>
          {(Object.keys(TYPE_LABELS) as InventoryMovementType[]).map((type) => (
            <option key={type} value={type}>{TYPE_LABELS[type]}</option>
          ))}
        </select>
        <button
          onClick={applyFilter}
          className="rounded-2xl bg-cyan-400 px-5 py-3 font-black text-slate-950 hover:bg-cyan-300"
        >
          Apply
        </button>
        <button
          onClick={clearFilter}
          className="rounded-2xl border border-slate-700 px-5 py-3 font-black text-slate-300 hover:border-cyan-400 hover:text-white"
        >
          Reset
        </button>
      </div>

      {error && (
        <p className="mt-5 rounded-2xl border border-rose-500/30 bg-rose-500/10 p-4 font-bold text-rose-200">{error}</p>
      )}

      <div className="mt-6 overflow-hidden rounded-3xl border border-slate-800 bg-slate-950">
        <table className="min-w-full divide-y divide-slate-800 text-sm">
          <thead className="bg-slate-900/60 text-left text-xs font-black uppercase tracking-widest text-slate-400">
            <tr>
              <th className="px-4 py-3">Date</th>
              <th className="px-4 py-3">Type</th>
              <th className="px-4 py-3">Product</th>
              <th className="px-4 py-3">SKU</th>
              <th className="px-4 py-3 text-right">Change</th>
              <th className="px-4 py-3 text-right">Stock after</th>
              <th className="px-4 py-3">Order</th>
              <th className="px-4 py-3">Reason</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800 text-slate-200">
            {isLoading && (
              <tr>
                <td colSpan={8} className="px-4 py-10 text-center text-slate-400">Loading movements...</td>
              </tr>
            )}
            {!isLoading && !error && filtered.length === 0 && (
              <tr>
                <td colSpan={8} className="px-4 py-10 text-center text-slate-400">No inventory movements found.</td>
              </tr>
            )}
            {filtered.map((movement) => (
              <tr key={movement.id} className="hover:bg-slate-900/40">
                <td className="whitespace-nowrap px-4 py-3 text-slate-400">{new Date(movement.createdAt).toLocaleString()}</td>
                <td className="px-4 py-3">
                  <span className={`rounded-full px-3 py-1 text-[10px] font-black uppercase tracking-widest ${TYPE_STYLES[movement.type]}`}>
                    {TYPE_LABELS[movement.type]}
                  </span>
                </td>
                <td className="px-4 py-3 font-bold text-white">
                  <span className="block">{movement.productName}</span>
                  <span className="text-xs text-slate-500">ID #{movement.productId}</span>
                </td>
                <td className="px-4 py-3 text-slate-400">{movement.productSku}</td>
                <td className={`whitespace-nowrap px-4 py-3 text-right font-black ${movement.quantityChange > 0 ? "text-emerald-300" : movement.quantityChange < 0 ? "text-rose-300" : "text-slate-300"}`}>
                  {movement.quantityChange > 0 ? `+${movement.quantityChange}` : movement.quantityChange}
                </td>
                <td className="whitespace-nowrap px-4 py-3 text-right font-bold text-white">{movement.stockAfter}</td>
                <td className="px-4 py-3 text-slate-400">{movement.orderId ? `#${movement.orderId}` : "-"}</td>
                <td className="px-4 py-3 text-slate-400">{movement.reason}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
