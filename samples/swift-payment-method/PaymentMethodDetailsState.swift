struct PaymentMethodDetailsState {

    private let method: API.PaymentMethod

    private var isMakingDefault = false
    private var isRemoving = false
    private var output: PaymentMethodDetailsViewController.OutputType?

    // MARK: - Init

    init(method: API.PaymentMethod) {
        self.method = method
    }

    // MARK: - Changes

    enum Event {
        case tappedMakeDefault
        case madeDefault(updatedUser: API.User?)
        case tappedRemove
        case didRemove(updatedUser: API.User?)
        case producedOutput
    }

    static func reduce(state: PaymentMethodDetailsState, event: Event) -> PaymentMethodDetailsState {

        var result = state

        switch event {
        case .tappedMakeDefault:
            result.isMakingDefault = true
        case .madeDefault(let updatedUser):
            result.isMakingDefault = false
            result.output = updatedUser.flatMap(PaymentMethodDetailsCompleted.init)
        case .tappedRemove:
            result.isRemoving = true
        case .didRemove(let updatedUser):
            result.isRemoving = false
            result.output = updatedUser.flatMap(PaymentMethodDetailsCompleted.init)
        case .producedOutput:
            result.output = nil
        }

        return result
    }

    // MARK: - Queries

    // MARK: UI

    var viewState: PaymentMethodDetailsView.ViewState {
        return .init(title: method.creditCard?.cardTypeName ?? L10n.paymentMethodDirectDebit(),
                     isDefaultPayment: method.isDefault)
    }

    var paymentSection: API.PaymentMethod {
        return method
    }

    var isEditActionsAvailable: Bool {
        return !method.isDefault
    }

    var isLoadingVisible: Bool {
        return isMakingDefault
    }

    // MARK: Data

    var makeDefault: API.UpdatePaymentMethodRequest? {
        guard isMakingDefault else { return nil }
        return API.UpdatePaymentMethodRequest(defaultMethodId: method.id)
    }

    var removeAfterConfirmation: API.RemovePaymentMethodRequest? {
        guard isRemoving else { return nil }
        return API.RemovePaymentMethodRequest(methodId: method.id)
    }

    // MAKR: Flow

    var produce: PaymentMethodDetailsCompleted? {
        return output
    }
}
