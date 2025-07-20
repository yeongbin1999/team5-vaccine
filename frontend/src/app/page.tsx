import Link from 'next/link';
import Image from 'next/image';
import { ArrowRight, Coffee, Truck, Shield } from 'lucide-react';

export default function Home() {
  return (
    <div className="min-h-screen bg-[#f8f5f2]">
      {/* 헤더 아래 카페 이미지 */}
      <div className="relative w-full">
        <div className="w-full h-[260px] md:h-[340px] lg:h-[420px] overflow-hidden">
          <Image
            src="/cafe.png"
            alt="Grids & Circles"
            fill
            className="object-cover"
            priority
            sizes="100vw"
            style={{ objectPosition: 'center' }}
          />
        </div>
      </div>

      {/* 메인 컨텐츠 */}
      <main className="bg-white w-full rounded-2xl shadow-sm p-4 md:p-8 mt-8">
        {/* 특징 섹션 */}
        <section className="py-8">
          <h2 className="text-3xl font-bold text-center text-brown-900 mb-12">
            왜 Grids & Circles를 선택해야 할까요?
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="text-center">
              <div className="w-16 h-16 bg-amber-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <Coffee className="w-8 h-8 text-amber-600" />
              </div>
              <h3 className="text-xl font-semibold text-brown-900 mb-2">
                프리미엄 원두
              </h3>
              <p className="text-brown-700">
                엄선된 최고급 원두만을 사용하여 깊고 풍부한 맛을 제공합니다.
              </p>
            </div>
            <div className="text-center">
              <div className="w-16 h-16 bg-amber-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <Truck className="w-8 h-8 text-amber-600" />
              </div>
              <h3 className="text-xl font-semibold text-brown-900 mb-2">
                빠른 배송
              </h3>
              <p className="text-brown-700">
                2시 이전 주문 시 당일 발송해드립니다.
              </p>
            </div>
            <div className="text-center">
              <div className="w-16 h-16 bg-amber-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <Shield className="w-8 h-8 text-amber-600" />
              </div>
              <h3 className="text-xl font-semibold text-brown-900 mb-2">
                품질 보장
              </h3>
              <p className="text-brown-700">
                만족하지 못하시면 언제든지 환불해드리는 품질 보장 서비스입니다.
              </p>
            </div>
          </div>
        </section>

        {/* 노란 배경 섹션 */}
        <section className="bg-gradient-to-br from-amber-50 to-orange-100 py-16 rounded-2xl mt-8">
          <div className="text-center max-w-4xl mx-auto">
            <h2 className="text-4xl md:text-5xl font-bold text-brown-900 mb-6">
              <span className="text-amber-600">Grids & Circles</span>
            </h2>
            <p className="text-xl text-brown-700 mb-8">
              엄선된 원두로 만든 프리미엄 커피를 편리하게 주문하고 <br />
              신선한 맛을 그대로 느껴보세요
            </p>
            <Link
              href="/shop"
              className="inline-flex items-center bg-amber-200 text-amber-900 px-8 py-4 rounded-lg text-lg font-semibold hover:bg-amber-300 transition-colors"
            >
              상품 보기
              <ArrowRight className="ml-2 w-5 h-5" />
            </Link>
          </div>
        </section>
      </main>
    </div>
  );
}
