import { createApiClient } from "@mgmz/api-client";
import { formatCurrency } from "@mgmz/shared";

export default async function ProductsPage() {
  const products = await createApiClient().products.list({ size: 50 }).catch(() => ({ items: [] }));

  return (
    <div>
      <div className="flex items-center justify-between gap-4">
        <h1 className="text-4xl font-black">Products</h1>
        <button className="rounded-2xl bg-cyan-400 px-4 py-3 font-black text-slate-950">New product</button>
      </div>
      <div className="mt-6 overflow-hidden rounded-3xl border border-slate-800 bg-slate-950">
        {products.items.map((product) => (
          <div key={product.id} className="grid gap-3 border-b border-slate-800 p-4 md:grid-cols-[1fr_160px_120px]">
            <div><p className="font-black text-white">{product.name}</p><p className="text-sm text-slate-400">{product.sku}</p></div>
            <p className="font-bold text-cyan-300">{formatCurrency(product.price)}</p>
            <p className="text-sm text-slate-300">Stock {product.stock}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
