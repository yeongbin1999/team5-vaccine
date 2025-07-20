export function Footer() {
  return (
    <footer className="bg-[#FFF8F0] text-gray-700">
      <div className="container mx-auto px-4 py-4 w-full">
        {/* 하단 구분선 */}
        <div className="border-t border-gray-400 mt-4 pt-4">
          <div className="flex flex-col md:flex-row justify-between items-center">
            <p className="text-gray-400 text-sm">
              © 2025 Grids & Circles CAFE
            </p>
          </div>
        </div>
      </div>
    </footer>
  );
}
