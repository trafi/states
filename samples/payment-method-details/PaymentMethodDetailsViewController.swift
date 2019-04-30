private typealias S = PaymentMethodDetailsState
private typealias E = S.Event
private typealias Feedback = (Driver<S>) -> Signal<E>

struct PaymentMethodDetailsCompleted {
    let currentUser: API.User
}

class PaymentMethodDetailsViewController: LayoutViewController<PaymentMethodDetailsView>, IO {

    typealias InputType = PaymentMethodDetailsState
    typealias OutputType = PaymentMethodDetailsCompleted

    // MARK: -

    override func viewDidLoad() {
        super.viewDidLoad()

        Driver.system(initialState: input,
                      reduce: S.reduce,
                      feedback: bindUI() + bindData() + bindFlow())
            .drive()
            .disposed(by: rx_disposeBag)
    }

    // MARK: UI

    private func bindUI() -> [Feedback] {
        return [
            bindTable(),
            bindViewState(),
            bindIsLoadingVisible(),
        ]
    }

    private func bindTable() -> Feedback {
        return bind(layoutView.tableView) { tableView, state in

            let detailsSection = state.query { $0.paymentSection }
                .map { $0.detailsSection }

            let actionsSection: Driver<RxTableSection<CellData>?> = state.query { $0.isEditActionsAvailable }
                .map { $0 ? RxTableSection(elements: [MakeDefaultCell(), RemoveCell()]) : nil }

            let tableData = RxTableDataSource(for: tableView)
            let table = Driver.combineLatest(detailsSection, actionsSection) { .sections([$0, $1].compactMap { $0 }) }
                .drive(tableView.rx.items(dataSource: tableData))

            let tappedMakeDefault = tableData.rx.modelSelected(MakeDefaultCell.self)
                .asSignal { _ in E.tappedMakeDefault }
            let tappedRemove = tableData.rx.modelSelected(RemoveCell.self)
                .asSignal { _ in E.tappedRemove }

            return Bindings(subscriptions: [table], events: [tappedMakeDefault, tappedRemove])
        }
    }

    private func bindViewState() -> Feedback {
        return bind(layoutView) { view, state in

            let viewState = state.query { $0.viewState }
                .driveNext { view.viewState = $0 }

            return Bindings(subscriptions: [viewState], events: [.empty()])
        }
    }

    private func bindIsLoadingVisible() -> Feedback {
        return bind(self) { `self`, state in

            let popup = ActivityPopupLayout()

            let isLoadingVisible = state.query { $0.isLoadingVisible }
                .driveNext { popup.display(in: self, isVisible: $0) }

            return Bindings(subscriptions: [isLoadingVisible], events: [Signal<E>]())
        }
    }

    // MARK: Data

    private func bindData() -> [Feedback] {
        return [
            makeDefault(),
            removeAfterConfirmation(),
        ]
    }

    private func makeDefault() -> Feedback {
        return react(query: { $0.makeDefault }) {
            API.V1UsersPaymentDefaultPost(request: $0).request()
                .map(E.madeDefault)
                .asSignal(onErrorJustReturn: .madeDefault(updatedUser: nil))
        }
    }

    private func removeAfterConfirmation() -> Feedback {
        return react(query: { $0.removeAfterConfirmation }) { [unowned self] req in
            self.tr.confirmation(title: L10n.paymentMethodRemoveConfirmation(),
                                 confirmTitle: L10n.actionRemove(),
                                 action: { API.V1UsersPaymentRemovePost(request: req).request() }
                                 )
                .map(E.didRemove)
                .asSignal(onErrorJustReturn: E.didRemove(updatedUser: nil))
        }
    }

    // MARK: Flow

    private func bindFlow() -> [Feedback] {
        return [
            produce(),
        ]
    }

    private func produce() -> Feedback {
        return react(query: { $0.produce }) { [unowned self] in
            self.produce($0)
            return .just(E.producedOutput)
        }
    }
}
