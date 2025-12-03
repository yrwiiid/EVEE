package com.example.evee;

public class ApiConfig {
    // Ganti dengan URL ngrok atau server lokal kamu
    public static final String BASE_URL = "https://lemuel-unsatisfiable-empathetically.ngrok-free.dev/EVEE0.2/api/";
    public static final String LOGIN_URL = BASE_URL + "auth_login.php";
    public static final String REGISTER_URL = "https://lemuel-unsatisfiable-empathetically.ngrok-free.dev/EVEE0.2/api/auth_register.php";
    public static final String SCREENING_URL = BASE_URL + "user_profile.php";
    public static final String HOME_URL = BASE_URL + "home_dashboard.php";
    public static final String CALENDAR_URL = BASE_URL + "period_cycles.php";
}
