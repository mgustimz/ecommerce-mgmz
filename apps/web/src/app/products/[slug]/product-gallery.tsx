"use client";

import { useState } from "react";

type Props = {
  images: string[];
  alt: string;
};

export function ProductGallery({ images, alt }: Props) {
  const [active, setActive] = useState(0);
  const total = images.length;
  const current = images[active] ?? null;
  const previous = () => setActive((index) => (index - 1 + total) % total);
  const next = () => setActive((index) => (index + 1) % total);

  return (
    <div>
      <div className="relative aspect-square overflow-hidden rounded-lg bg-neutral-100">
        {current ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img
            key={current}
            src={current}
            alt={alt}
            className="h-full w-full object-cover"
          />
        ) : (
          <div className="grid h-full place-items-center text-sm font-bold uppercase tracking-wider text-neutral-300">
            No image
          </div>
        )}

        {total > 1 && (
          <>
            <button
              type="button"
              onClick={previous}
              aria-label="Previous image"
              className="absolute left-2 top-1/2 grid h-10 w-10 -translate-y-1/2 place-items-center rounded-full bg-white/90 text-base font-black text-neutral-800 shadow transition hover:bg-white"
            >
              ‹
            </button>
            <button
              type="button"
              onClick={next}
              aria-label="Next image"
              className="absolute right-2 top-1/2 grid h-10 w-10 -translate-y-1/2 place-items-center rounded-full bg-white/90 text-base font-black text-neutral-800 shadow transition hover:bg-white"
            >
              ›
            </button>
            <span className="absolute bottom-3 right-3 rounded-full bg-neutral-900/80 px-3 py-1 text-xs font-black text-white">
              {active + 1} / {total}
            </span>
          </>
        )}
      </div>

      {total > 1 && (
        <div className="mt-3 grid grid-cols-4 gap-2">
          {images.map((url, index) => (
            <button
              type="button"
              key={url}
              onClick={() => setActive(index)}
              aria-label={`Show image ${index + 1}`}
              aria-current={index === active ? "true" : undefined}
              className={`overflow-hidden rounded border bg-neutral-50 transition ${
                index === active
                  ? "border-[3px] border-red-600 p-0"
                  : "border-neutral-200 hover:border-red-600"
              }`}
            >
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img
                src={url}
                alt={alt}
                className="aspect-square w-full object-cover"
              />
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
