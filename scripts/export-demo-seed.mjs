// Xuất dữ liệu demo (cùng nguồn với bản giao diện: restaurant-manager-fe/src/mocks/seed.js) thành Flyway SQL.
// Chạy trong thư mục restaurant-manager-fe (cần vite-node để đọc import kiểu Vite):
//   npx vite-node ../restaurant-manager-be/scripts/export-demo-seed.mjs
// Kết quả: restaurant-manager-be/src/main/resources/db/demo/V2__demo_seed.sql (chỉ hồ sơ dev nạp thư mục db/demo).
// Ngày giờ trong dữ liệu tính theo lúc chạy script; chạy lại trước buổi demo để "hôm nay" có đơn đang mở.
import { writeFileSync } from 'node:fs'
import { buildSeed } from '../../restaurant-manager-fe/src/mocks/seed.js'

// Mật khẩu demo của mọi tài khoản: 123456 (BCrypt cost 11). Chỉ dùng cho dữ liệu demo.
const DEMO_HASH = '$2b$11$x48xZ5PHaFMYw8rxdoJ35uVk1gYafMQtkqITzanGolRcAocuEuUqe'

// tập FE -> [bảng, các cột theo tên trường FE, đổi tên cột]
const TABLES = [
  ['roles', 'roles', ['id', 'code', 'name', 'description', 'isSystem', 'locked', 'fixed']],
  ['tags', 'tags', ['id', 'name', 'type']],
  ['users', 'users', ['id', 'kind', 'roleId', 'fullName', 'position', 'email', 'phone', 'passwordHash', 'status', 'workStatus', 'hireDate', 'gender', 'points', 'createdAt', 'lastLoginAt']],
  ['categories', 'categories', ['id', 'name', 'sortOrder', 'isActive']],
  ['ingredients', 'ingredients', ['id', 'name', 'unit', 'stock', 'minStock', 'cost']],
  ['items', 'items', ['id', 'categoryId', 'name', 'description', 'price', 'image', 'isAvailable', 'spicyLevel', 'prepMinutes']],
  ['floors', 'floors', ['id', 'name', 'level', 'isActive']],
  ['areas', 'areas', ['id', 'floorId', 'name', 'type', 'serviceFeePercent', 'minSpend', 'isActive', 'planW', 'planH']],
  ['tables', 'dining_tables', ['id', 'areaId', 'code', 'seats', 'status', 'isActive', 'qrToken', 'shape', 'posX', 'posY']],
  ['vouchers', 'vouchers', ['id', 'code', 'name', 'type', 'value', 'maxDiscount', 'minOrderAmount', 'startAt', 'endAt', 'usageLimit', 'usedCount', 'isActive']],
  ['shifts', 'shifts', ['id', 'name', 'start', 'end'], { start: 'start_time', end: 'end_time' }],
  ['schedules', 'schedules', ['id', 'employeeId', 'shiftId', 'areaId', 'date', 'status'], { date: 'work_date' }],
  ['reservations', 'reservations', ['id', 'customerId', 'guestName', 'guestPhone', 'time', 'guestCount', 'areaId', 'note', 'status', 'createdAt']],
  ['orders', 'orders', ['id', 'tableId', 'reservationId', 'customerId', 'openedBy', 'type', 'guestCount', 'status', 'openedAt', 'closedAt']],
  ['orderItems', 'order_items', ['id', 'orderId', 'itemId', 'qty', 'unitPrice', 'note', 'status', 'source', 'createdAt']],
  ['bills', 'bills', ['id', 'orderId', 'voucherId', 'subtotal', 'serviceFee', 'minSpendTopUp', 'discount', 'pointsAmount', 'tax', 'total', 'pointsUsed', 'pointsEarned', 'method', 'status', 'cashierId', 'paidAt']],
  ['inventoryTx', 'inventory_transactions', ['id', 'ingredientId', 'type', 'qty', 'unitCost', 'note', 'employeeId', 'createdAt']],
  ['serviceRequests', 'service_requests', ['id', 'tableId', 'orderId', 'type', 'status', 'handledBy', 'createdAt', 'ackAt', 'doneAt']],
  ['reviews', 'reviews', ['id', 'customerId', 'itemId', 'rating', 'comment', 'hidden', 'createdAt']],
  ['chatSessions', 'chat_sessions', ['id', 'customerId', 'title', 'turns', 'feedback', 'addedToCart', 'latencyMs', 'createdAt']],
]
const snake = (s) => s.replace(/[A-Z]/g, (c) => '_' + c.toLowerCase())
// undefined: FE không có trường này, dùng DEFAULT của cột; null tường minh mới là NULL
const sql = (v) => (v === undefined ? 'DEFAULT' : v === null ? 'NULL' : typeof v === 'boolean' || typeof v === 'number' ? String(v) : `'${String(v).replaceAll("'", "''")}'`)

const db = buildSeed()
for (const u of db.users) {
  if (u.kind === 'CUSTOMER' || u.roleId) u.passwordHash = DEMO_HASH // bếp, phục vụ (roleId null) không có tài khoản
  u.points ??= 0
}
const out = ['-- Sinh tự động bởi scripts/export-demo-seed.mjs. Dữ liệu GIẢ chỉ để demo; mật khẩu mọi tài khoản: 123456.', '']
const insert = (table, cols, rows) => {
  for (let i = 0; i < rows.length; i += 500) {
    out.push(`insert into ${table} (${cols.join(', ')}) values`)
    out.push(rows.slice(i, i + 500).map((r) => `(${r.map(sql).join(', ')})`).join(',\n') + ';')
  }
}
for (const [col, table, fields, rename = {}] of TABLES) {
  const rows = db[col] ?? []
  if (!rows.length) continue
  insert(table, fields.map((f) => rename[f] ?? snake(f)), rows.map((r) => fields.map((f) => r[f])))
  if (table !== 'chat_sessions') out.push(`select setval(pg_get_serial_sequence('${table}', 'id'), (select max(id) from ${table}));`)
}
// bảng nối (các trường mảng của FE)
insert('role_permissions', ['role_id', 'permission'], db.roles.flatMap((r) => r.permissions.map((p) => [r.id, p])))
insert('item_tags', ['item_id', 'tag_id'], db.items.flatMap((i) => i.tagIds.map((t) => [i.id, t])))
insert('item_recipe', ['item_id', 'ingredient_id', 'qty'], db.items.flatMap((i) => i.recipe.map((x) => [i.id, x.ingredientId, x.qty])))
insert('reservation_tables', ['reservation_id', 'table_id'], db.reservations.flatMap((r) => r.tableIds.map((t) => [r.id, t])))
const allergy = db.users.flatMap((u) => (u.allergyTagIds ?? []).map((t) => [u.id, t]))
if (allergy.length) insert('user_allergy_tags', ['user_id', 'tag_id'], allergy)

const path = new URL('../src/main/resources/db/demo/V2__demo_seed.sql', import.meta.url)
writeFileSync(path, out.join('\n') + '\n')
console.log('Đã ghi', path.pathname, Object.fromEntries(TABLES.map(([c]) => [c, db[c]?.length ?? 0])))
