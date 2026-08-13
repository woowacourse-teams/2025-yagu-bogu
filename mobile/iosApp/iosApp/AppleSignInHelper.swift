import AuthenticationServices
import ComposeApp
import UIKit

/// Kotlin `AppleSignInDelegate` 인터페이스의 Swift 구현체.
final class SwiftAppleSignInDelegate: NSObject, AppleSignInDelegate {

    private var onSuccess: ((String) -> Void)?
    private var onCancel: (() -> Void)?
    private var onFailure: ((String) -> Void)?

    func signIn(
        onSuccess: @escaping (String) -> Void,
        onCancel: @escaping () -> Void,
        onFailure: @escaping (String) -> Void
    ) {
        self.onSuccess = onSuccess
        self.onCancel = onCancel
        self.onFailure = onFailure

        DispatchQueue.main.async {
            let provider = ASAuthorizationAppleIDProvider()
            let request = provider.createRequest()
            request.requestedScopes = [.fullName, .email]

            let controller = ASAuthorizationController(authorizationRequests: [request])
            controller.delegate = self
            controller.presentationContextProvider = self
            controller.performRequests()
        }
    }
}

// MARK: - ASAuthorizationControllerDelegate

extension SwiftAppleSignInDelegate: ASAuthorizationControllerDelegate {

    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization authorization: ASAuthorization
    ) {
        guard
            let credential = authorization.credential as? ASAuthorizationAppleIDCredential,
            let identityTokenData = credential.identityToken,
            let idToken = String(data: identityTokenData, encoding: .utf8)
        else {
            onFailure?("ID 토큰을 가져올 수 없습니다.")
            clearCallbacks()
            return
        }

        onSuccess?(idToken)
        clearCallbacks()
    }

    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError error: Error
    ) {
        let authError = error as? ASAuthorizationError
        if authError?.code == .canceled {
            onCancel?()
        } else {
            onFailure?(error.localizedDescription)
        }
        clearCallbacks()
    }
}

// MARK: - ASAuthorizationControllerPresentationContextProviding

extension SwiftAppleSignInDelegate: ASAuthorizationControllerPresentationContextProviding {

    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first?
            .windows
            .first ?? ASPresentationAnchor()
    }
}

// MARK: - Private

extension SwiftAppleSignInDelegate {

    private func clearCallbacks() {
        onSuccess = nil
        onCancel = nil
        onFailure = nil
    }
}
