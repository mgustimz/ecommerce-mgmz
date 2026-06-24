import { createApiClient } from "@mgmz/api-client";
import { formatCurrency } from "@mgmz/shared";
import { AddToCartButton } from "./add-to-cart-button";

type Props = {
  params: Promise<{ slug: string }>;
};

export default async function ProductDetailPage({ params }: Props) {
  const { slug } = await params;
  const product = await createApiClient().products.getBySlug(slug);

  return (
    <div className="mx-auto grid max-w-6xl gap-10 px-5 py-12 md:grid-cols-2">
      <div className="aspect-square rounded-[2rem] bg-white p-6 shadow-sm">
        <div className="h-full rounded-[1.5rem] bg-stone-100" />
      </div>
      <div>
        <p className="text-sm font-bold uppercase tracking-[0.24em] text-amber-700">{product.categoryName ?? "Product"}</p>
        <h1 className="mt-4 text-5xl font-black tracking-tight">{product.name}</h1>
        <p className="mt-4 text-3xl font-black text-amber-700">{formatCurrency(product.price)}</p>
        <p className="mt-6 leading-8 text-stone-700">{product.description ?? "No description yet."}</p>
        <div className="mt-8 rounded-3xl bg-white p-5 shadow-sm">
          <p className="font-bold">Stock: {product.stock}</p>
          <p className="mt-1 text-sm text-stone-500">SKU: {product.sku}</p>
        </div>
        <AddToCartButton productId={product.id} />
      </div>
    </div>
  );
}
