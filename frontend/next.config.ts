import type { NextConfig } from 'next';

const nextConfig: NextConfig = {
  images: {
    domains: ['dummyimg.com'],
  },
  async rewrites() {
    return [
      {
        source: '/api/v1/:path*',
        destination: 'http://localhost:8080/api/v1/:path*',
      },
    ];
  },
};

export default nextConfig;
