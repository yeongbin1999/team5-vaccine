'use client';

import React from 'react';
import { ProductGrid } from './components/ProductGrid';

export default function ShopPage() {
  return (
    <div className="flex-1 bg-gray-50 py-8">
      <div className="container mx-auto px-4">
        <ProductGrid />
      </div>
    </div>
  );
}
