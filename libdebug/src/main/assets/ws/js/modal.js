$(function( $ ) {

var $modal = $( '#myModal' );

var modal_func = null;
var $modal_btn = $modal.find( '#btn_yes' );

// show dialog
function showModal( data, func ) {

if(data.tip)
    $modal.find( '#modal_tip' ).html( data.tip );
    if(data.msg)
    $modal.find( '#modal_msg' ).html( data.msg );
    if(data.btn_yes)
            $modal.find( '#btn_yes' ).html( data.btn_yes );
    if(data.btn_no)
        $modal.find( '#btn_no' ).html( data.btn_no );

$modal.modal({
keyboard: true
})
if(func){
modal_func = func;
$modal_btn.show();
}
else{
$modal_btn.hide();
}

}

function modalMessage( message ) {
    var data = {
        'msg': message,
        'btn_no': MODAL_MSG.close
    }
    showModal(data);
}

// yes
$modal_btn.click(function() {
    if ( modal_func ) {
        modal_func();
    }
//    cancelDialog();
    $modal.modal('hide');
});

$.extend({
    showModal: showModal,
    modalMessage: modalMessage
});

});
