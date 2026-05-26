import ComposeApp
import FirebaseCore
import UIKit

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // Firebase 먼저 초기화
        FirebaseApp.configure()

        // Koin 초기화 (GeofenceObserver createdAtStart=true 이므로 여기서 실행되어야 필요)
        KoinInitializerKt.setup(
            googleSignInDelegate: SwiftGoogleSignInDelegate(),
            appleSignInDelegate: SwiftAppleSignInDelegate()
        )

        if launchOptions?[.location] != nil {
            print("앱이 지오펜스 이벤트로 재실행됨")
        }

        return true
    }
}
