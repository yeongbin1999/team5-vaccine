import { Order } from './types';

export const orders: Order[] = [
  {
    id: '20240601-001',
    date: '2024-06-01',
    status: '배송완료',
    total: 18500,
    items: [
      { name: '에티오피아 원두 200g', qty: 1 },
      { name: '콜드브루 500ml', qty: 2 },
    ],
  },
  {
    id: '20240528-002',
    date: '2024-05-28',
    status: '배송중',
    total: 12000,
    items: [{ name: '케냐 원두 200g', qty: 1 }],
  },
];
