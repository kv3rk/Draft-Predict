window.LegacyDraftConfig = (function () {
    const BAN_ORDER_BLUE_FIRST = ['blue-1', 'red-1', 'blue-2', 'red-2', 'blue-3', 'red-3'];
    const BAN_ORDER_RED_FIRST  = ['red-1', 'blue-1', 'red-2', 'blue-2', 'red-3', 'blue-3'];

    const PICK_ORDER_BLUE_FIRST = ['blue-1', 'red-1', 'red-2', 'blue-2', 'blue-3', 'red-3'];
    const PICK_ORDER_RED_FIRST  = ['red-1', 'blue-1', 'blue-2', 'red-2', 'red-3', 'blue-3'];

    const BAN_ORDER_SECOND_BLUE_FIRST = ['red-4', 'blue-4', 'red-5', 'blue-5'];
    const BAN_ORDER_SECOND_RED_FIRST  = ['blue-4', 'red-4', 'blue-5', 'red-5'];

    const PICK_ORDER_SECOND_BLUE_FIRST = ['red-4', 'blue-4', 'blue-5', 'red-5'];
    const PICK_ORDER_SECOND_RED_FIRST  = ['blue-4', 'red-4', 'red-5', 'blue-5'];

    return {
        type: 'LEGACY',
        getBanOrder: function (firstPickSide) {
            return firstPickSide === 'BLUE' ? [...BAN_ORDER_BLUE_FIRST] : [...BAN_ORDER_RED_FIRST];
        },
        getPickOrder: function (firstPickSide) {
            return firstPickSide === 'BLUE' ? [...PICK_ORDER_BLUE_FIRST] : [...PICK_ORDER_RED_FIRST];
        },
        getSecondBanOrder: function (firstPickSide) {
            return firstPickSide === 'BLUE' ? [...BAN_ORDER_SECOND_BLUE_FIRST] : [...BAN_ORDER_SECOND_RED_FIRST];
        },
        getSecondPickOrder: function (firstPickSide) {
            return firstPickSide === 'BLUE' ? [...PICK_ORDER_SECOND_BLUE_FIRST] : [...PICK_ORDER_SECOND_RED_FIRST];
        },
        hasSecondPhase: function () {
            return true;
        }
    };
})();