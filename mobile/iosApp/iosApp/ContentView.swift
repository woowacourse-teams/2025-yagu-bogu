import UIKit
import SwiftUI
import ComposeApp

/// Compose UI를 SwiftUI 계층에 삽입하는 브릿지 뷰.
struct ComposeView: UIViewControllerRepresentable {

    func makeUIViewController(context: Context) -> UIViewController {
        // GoogleSignInDelegate, AppleSignInDelegate를 생성해 Kotlin DI(Koin)에 주입
        MainViewControllerKt.MainViewController(
            googleSignInDelegate: SwiftGoogleSignInDelegate(),
            appleSignInDelegate: SwiftAppleSignInDelegate()
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea() // safe area는 Compose가 직접 처리
    }
}
