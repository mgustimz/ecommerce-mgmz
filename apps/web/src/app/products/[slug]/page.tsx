import { createApiClient } from "@mgmz/api-client";
import { formatCurrency } from "@mgmz/shared";
import { AddToCartButton } from "./add-to-cart-button";
import { ProductGallery } from "./product-gallery";
import Link from "next/link";

type Props = {
  params: Promise<{ slug: string }>;
};

export default async function ProductDetailPage({ params }: Props) {
  const { slug } = await params;
  const product = await createApiClient().products.getBySlug(slug);
  const isOutOfStock = product.stock <= 0;

  const price = Number(product.price);
  const originalPrice = product.originalPrice != null ? Number(product.originalPrice) : null;
  const hasDiscount = originalPrice !== null && originalPrice > price;
  const discountPercent = hasDiscount ? Math.round(((originalPrice - price) / originalPrice) * 100) : 0;

  const rating = Number(product.averageRating ?? 0);
  const reviewCount = product.reviewCount ?? 0;
  const fullStars = Math.round(rating);

  return (
    <div className="mx-auto max-w-6xl px-5 py-8">
      <nav className="mb-4 text-xs text-neutral-500">
        <Link href="/" className="hover:text-red-600">Home</Link> /{" "}
        <Link href="/products" className="hover:text-red-600">Products</Link> /{" "}
        <span>{product.name}</span>
      </nav>

      <div className="grid gap-8 rounded-lg border border-neutral-200 bg-white p-6 md:grid-cols-2">
        <ProductGallery images={product.imageUrls} alt={product.name} />

        <div>
          <p className="text-xs font-bold uppercase tracking-wider text-neutral-500">{product.categoryName ?? "Product"}</p>
          <h1 className="mt-2 text-3xl font-black">{product.name}</h1>
          <div className="mt-3 flex items-center gap-3 text-sm">
            <span className="star-row" aria-label={`${rating.toFixed(1)} out of 5`}>
              {"★".repeat(fullStars)}{"☆".repeat(5 - fullStars)}
            </span>
            <span className="text-neutral-500">{reviewCount > 0 ? `${rating.toFixed(1)} (${reviewCount} ${reviewCount === 1 ? "review" : "reviews"})` : "No reviews yet"}</span>
            <span className="text-neutral-300">|</span>
            <span className="text-neutral-500">SKU: {product.sku}</span>
          </div>
          <div className="mt-5 flex items-baseline gap-3">
            <p className="text-3xl font-black" style={{ color: "#cc1d00" }}>{formatCurrency(product.price)}</p>
            {hasDiscount && (
              <>
                <p className="text-lg text-neutral-400 line-through">{formatCurrency(originalPrice)}</p>
                <span className="rounded px-2 py-0.5 text-xs font-black uppercase text-white" style={{ background: "#cc1d00" }}>
                  -{discountPercent}%
                </span>
              </>
            )}
          </div>
          <p className={`mt-2 text-sm font-bold ${isOutOfStock ? "text-neutral-500" : "text-emerald-600"}`}>
            {isOutOfStock ? "Out of stock" : `Ready stock: ${product.stock}`}
          </p>

          <div className="mt-6 rounded-lg border border-neutral-200 bg-neutral-50 p-4 text-sm text-neutral-600">
            <p className="font-bold text-neutral-800">Product details</p>
            <ul className="mt-2 list-disc space-y-1 pl-5">
              <li>Weight: {product.weightGram} g</li>
              <li>Dimensions: {product.lengthCm} x {product.widthCm} x {product.heightCm} cm</li>
              <li>Shipping category: {product.shippingCategory}</li>
              <li>Status: {product.status}</li>
            </ul>
          </div>

          <p className="mt-6 leading-relaxed text-neutral-700">{product.description ?? "No description yet."}</p>

          <AddToCartButton productId={product.id} disabled={isOutOfStock} />

          <div className="mt-8 grid gap-3 rounded-lg border border-neutral-200 bg-white p-4 text-sm">
            <div className="flex items-center gap-3">
              <span className="grid h-9 w-9 place-items-center rounded-full bg-neutral-100 text-base" style={{ color: "#cc1d00" }}>★</span>
              <div>
                <p className="font-bold">100% Original</p>
                <p className="text-xs text-neutral-500">Genuine products, manufacturer warranty.</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <span className="grid h-9 w-9 place-items-center rounded-full bg-neutral-100 text-base" style={{ color: "#cc1d00" }}>↻</span>
              <div>
                <p className="font-bold">7-day return</p>
                <p className="text-xs text-neutral-500">Easy returns within 7 days for unopened items.</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <span className="grid h-9 w-9 place-items-center rounded-full bg-neutral-100 text-base" style={{ color: "#cc1d00" }}>✓</span>
              <div>
                <p className="font-bold">Secure checkout</p>
                <p className="text-xs text-neutral-500">5 payment methods including COD &amp; QRIS.</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
