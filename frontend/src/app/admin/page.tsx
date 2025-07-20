export default function AdminDashboard() {
  return (
    <div className="min-h-screen bg-gray-100 p-8">
      <div className="max-w-7xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-900 mb-8">
          관리자 대시보드
        </h1>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-700 mb-2">
              총 주문
            </h3>
            <p className="text-3xl font-bold text-blue-600">1,234</p>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-700 mb-2">
              총 매출
            </h3>
            <p className="text-3xl font-bold text-green-600">₩12,345,678</p>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-700 mb-2">
              총 사용자
            </h3>
            <p className="text-3xl font-bold text-purple-600">567</p>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-700 mb-2">
              총 상품
            </h3>
            <p className="text-3xl font-bold text-orange-600">89</p>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">
            최근 주문
          </h2>
          <div className="text-gray-600">
            관리자 기능이 구현되었습니다.
            <br />
            여기에 주문 목록, 상품 관리, 사용자 관리 등의 기능이 추가됩니다.
          </div>
        </div>
      </div>
    </div>
  );
}
