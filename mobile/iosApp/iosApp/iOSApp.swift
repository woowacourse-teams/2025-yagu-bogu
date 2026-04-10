import SwiftUI
import ComposeApp
import GoogleSignIn

@main
struct iOSApp: App {
    init() {
        // 앱이 켜지는 순간(백그라운드 포함) ios Koin 주입 시작
        // 이 시점에 GeofenceControllerImpl(createdAtStart=true)이 생성되며 지오펜스 리스너 등록
        KoinHelperKt.doInitKoinIos(
            googleSignInDelegate: SwiftGoogleSignInDelegate(),
            appleSignInDelegate: SwiftAppleSignInDelegate()
        )
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
                // Google OAuth 콜백 URL 처리 (Info.plist의 REVERSED_CLIENT_ID 스킴으로 재진입 시 호출)
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}
