"use client";

import { createApiClient, type DashboardSummary } from "@mgmz/api-client";
import { formatCurrency } from "@mgmz/shared";
import { useEffect, useState } from "react";

function getToken() {
  const raw = localStorage.getItem("mgmz_admin_session");
  return raw ? JSON.parse(raw).token as string : null;
}

export default function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = getToken();
    if (!token) {
      setError("Login as admin to load dashboard data.");
      return;
    }
    createApiClient().admin.dashboardSummary(token).then(setSummary).catch((err) => setError(err instanceof Error ? err.message : "Failed to load dashboard"));
  }, []);

  return (
    <div>
      <h1 className="text-4xl font-black">Dashboard</h1>
      {error && <p className="mt-5 rounded-2xl border border-amber-400/30 bg-amber-400/10 p-4 font-bold text-amber-200">{error}</p>}
      <div className="mt-6 grid gap-4 md:grid-cols-4">
        <Stat label="Orders" value={summary?.totalOrders ?? "-"} />
        <Stat label="Paid orders" value={summary?.paidOrders ?? "-"} />
        <Stat label="Low stock" value={summary?.lowStockProducts ?? "-"} />
        <Stat label="Paid revenue" value={summary ? formatCurrency(summary.paidRevenue) : "-"} />
      </div>
    </div>
  );
}

function Stat({ label, value }: { label: string; value: string | number }) {
  return <div className="rounded-3xl border border-slate-800 bg-slate-950 p-5"><p className="text-sm text-slate-400">{label}</p><p className="mt-2 text-3xl font-black text-white">{value}</p></div>;
}
