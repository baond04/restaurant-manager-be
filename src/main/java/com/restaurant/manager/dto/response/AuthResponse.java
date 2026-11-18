package com.restaurant.manager.dto.response;

import java.util.Set;

/** Kết quả đăng nhập: access token (giữ trong bộ nhớ ở FE), người dùng và quyền. Refresh token đi riêng trong cookie HttpOnly. */
public record AuthResponse(String accessToken, UserResponse user, Set<String> permissions) {

}
