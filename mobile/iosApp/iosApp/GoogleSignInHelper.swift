import UIKit
import GoogleSignIn
import ComposeApp

/// Kotlin `GoogleSignInDelegate` 인터페이스의 Swift 구현체.
/// NSObject 상속이 필요한 이유: Kotlin 인터페이스가 ObjC 프로토콜로 컴파일되기 때문.
final class SwiftGoogleSignInDelegate: NSObject, GoogleSignInDelegate {

    /// GIDSignIn 초기 설정. Koin이 IosGoogleCredentialManager를 생성할 때 호출된다.
    func configure(iosClientId: String, serverClientId: String) {
        GIDSignIn.sharedInstance.configuration = GIDConfiguration(
            clientID: iosClientId,
            serverClientID: serverClientId
        )
    }

    /// Google 로그인 UI를 표시하고 결과를 콜백으로 전달한다.
    /// 각 콜백은 Kotlin 코루틴 continuation을 재개한다.
    func signIn(
        onSuccess: @escaping (String) -> Void,
        onCancel: @escaping () -> Void,
        onFailure: @escaping (String) -> Void
    ) {
        DispatchQueue.main.async { // GIDSignIn은 메인 스레드에서 호출해야 함
            guard let rootVC = self.rootViewController() else {
                onFailure("presentingViewController를 찾을 수 없습니다.")
                return
            }

            GIDSignIn.sharedInstance.signIn(withPresenting: rootVC) { result, error in
                if let error = error as NSError? {
                    // 사용자 취소와 실제 오류를 구분
                    if error.code == GIDSignInError.canceled.rawValue {
                        onCancel()
                    } else {
                        onFailure(error.localizedDescription)
                    }
                    return
                }

                guard let idToken = result?.user.idToken?.tokenString else {
                    onFailure("ID 토큰을 가져올 수 없습니다.")
                    return
                }

                onSuccess(idToken)
            }
        }
    }

    func signOut() {
        GIDSignIn.sharedInstance.signOut()
    }

    // MARK: - Private

    private func rootViewController() -> UIViewController? {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first?
            .windows
            .first?
            .rootViewController
    }
}
