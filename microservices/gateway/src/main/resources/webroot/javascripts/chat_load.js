var counter = 0;

//function getCsrfToken() {
//    const csrfToken = document.cookie.split('; ').find(row => row.startsWith('XSRF-TOKEN')).split('=')[1];
//    return csrfToken;
//}

document.getElementById('connect').onclick = function() {
    //var csrfToken = getCsrfToken();
    
    var options = {
       vertxbus_reconnect_attempts_max: Infinity, // Max reconnect attempts
       vertxbus_reconnect_delay_min: 1000, // Initial delay (in ms) before first reconnect attempt
       vertxbus_reconnect_delay_max: 5000, // Max delay (in ms) between reconnect attempts
       vertxbus_reconnect_exponent: 2, // Exponential backoff factor
       vertxbus_randomization_factor: 0.5 // Randomization factor between 0 and 1
       //, websocketOptions: {
       /// headers: {
       //   'X-XSRF-TOKEN': csrfToken
       // }
      //}       
    };
    
    var eb2 = new EventBus('/eventbus', options);
    eb2.enableReconnect(true);		

    eb2.onopen = function() {
        document.getElementById('response').innerText = 'Socket opened';
        // set a handler to receive a message
        
        eb2.registerHandler("chat.to.client", function (err, msg) {
            counter = counter + 1;
            document.getElementById("counter").innerText = counter;
        });

        eb2.send('chat.to.server', 'hello'
            //, {
            //headers: {
                //'X-CSRF-Token': csrfToken
            //}
        //}
        );        
        
    }
    // onmessage sẽ nhận tất cả các tin nhắn gửi đến client
    //eb2.onmessage = function(event) {
    //    document.getElementById('response').innerText = counter*1000;
    //};
    eb2.onclose = function() {
        document.getElementById('response').innerText = 'Socket closed';
    };
};

document.getElementById('process').onclick = function(event) {
    event.preventDefault(); // Prevent the default anchor behavior
    var xhr = new XMLHttpRequest();
    xhr.open('GET', '/api/process', true);
    // Xử lý kết quả trả về từ API
    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            if (xhr.status === 200) {
                // Điền kết quả vào innerText của response
                document.getElementById('response').innerText = xhr.responseText;
            } else {
                // Xử lý lỗi nếu có
                document.getElementById('response').innerText = 'Error: ' + xhr.status;
            }
        }
    };	  
    xhr.send();
};    