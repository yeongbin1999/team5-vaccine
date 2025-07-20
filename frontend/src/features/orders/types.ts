export interface OrderItem {
  name: string;
  qty: number;
}

export interface Order {
  id: string;
  date: string;
  status: string;
  total: number;
  items: OrderItem[];
}
