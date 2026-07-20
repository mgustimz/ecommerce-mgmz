import { formatCurrency } from "@mgmz/shared";
import Link from "next/link";
import type { Product } from "@mgmz/api-client";

export function ProductCard({ product }: { product: Product }) {
  const firstImage = product.imageUrls[0];
  const isOutOfStock = product.stock <= 0;

  const price = Number(product.price);
  const originalPrice = product.originalPrice != null ? Number(product.originalPrice) : null;
  const hasDiscount = originalPrice !== null && originalPrice > price;
  const discountPercent = hasDiscount ? Math.round(((originalPrice - price) / originalPrice) * 100) : 0;

  const rating = Number(product.averageRating ?? 0);
  const reviews = product.reviewCount ?? 0;
  const fullStars = Math.round(rating);

  return (
    <Link href={`/products/${product.slug}`} className="product-card">
      <div className="relative aspect-square overflow-hidden rounded bg-neutral-50">
        {firstImage ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img src={firstImage} alt={product.name} className="h-full w-full object-cover" />
        ) : (
          <div className="grid h-full place-items-center text-xs font-bold uppercase tracking-wider text-neutral-300">
            No image
          </div>
        )}
        {hasDiscount && (
          <span className="absolute left-2 top-2 rounded px-2 py-0.5 text-[10px] font-black uppercase text-white" style={{ background: "#cc1d00" }}>
            -{discountPercent}%
          </span>
        )}
        {isOutOfStock && (
          <span className="absolute right-2 top-2 rounded bg-neutral-900 px-2 py-0.5 text-[10px] font-black uppercase text-white">
            Out of stock
          </span>
        )}
      </div>
      <h3 className="line-clamp-2 min-h-12 text-sm font-bold leading-tight text-neutral-800">{product.name}</h3>
      <div className="flex items-baseline gap-2">
        <p className="text-base font-black" style={{ color: "#cc1d00" }}>{formatCurrency(product.price)}</p>
        {hasDiscount && (
          <p className="text-xs text-neutral-400 line-through">{formatCurrency(originalPrice)}</p>
        )}
      </div>
      <p className="text-xs text-neutral-500">SKU: {product.sku}</p>
      <p className={`text-xs font-bold ${isOutOfStock ? "text-neutral-500" : "text-emerald-600"}`}>
        {isOutOfStock ? "Out of stock" : "Ready stock"}
      </p>
      <div className="flex items-center gap-2 pt-1 text-xs text-neutral-500">
        <span className="star-row" aria-hidden>{"★".repeat(fullStars)}{"☆".repeat(5 - fullStars)}</span>
        <span>{reviews > 0 ? `${rating.toFixed(1)} (${reviews})` : "No reviews yet"}</span>
      </div>
    </Link>
  );
}
