document.addEventListener("DOMContentLoaded",async()=>{
    const paymentRows=document.getElementById("paymentRows");
    if(paymentRows){
        try{
            const data=await api("/paymnt/view-all");
            const rows=Array.isArray(data)?data:[];
            if(!rows.length){paymentRows.innerHTML='<tr><td colspan="7" class="empty">No payment records found.</td></tr>';}else{
                paymentRows.innerHTML=rows.map(p=>`<tr><td>${p.paymentId}</td><td>${p.bookingId}</td><td>₹${p.amount??0}</td><td>${p.paymentMode??""}</td><td>${p.transactionReference??""}</td><td><span class="badge">${p.paymentStatus??""}</span></td><td>${p.paymentDate??""}</td></tr>`).join("");
            }
        }catch(e){paymentRows.innerHTML=`<tr><td colspan="7" class="empty">${e.message}</td></tr>`;}
    }
    try{
        const revenue=await api("/paymnt/stats/revenue");
        const failed=await api("/paymnt/stats/failed");
        if(document.getElementById("revenue"))document.getElementById("revenue").textContent="₹"+(revenue??0);
        if(document.getElementById("failed"))document.getElementById("failed").textContent=failed??0;
    }catch(e){if(document.getElementById("revenue"))toast(e.message);}
    const pay=document.getElementById("paymentForm");
    if(pay)pay.addEventListener("submit",async e=>{
        e.preventDefault();
        try{
            await api("/paymnt/pay",{method:"POST",body:JSON.stringify({bookingId:Number(document.getElementById("bookingId").value),amount:Number(document.getElementById("amount").value),paymentMode:document.getElementById("paymentMode").value})});
            toast("Payment saved successfully");
            setTimeout(()=>location.href="index.html",500);
        }catch(err){toast(err.message);}
    });
    const refund=document.getElementById("refundForm");
    if(refund)refund.addEventListener("submit",async e=>{
        e.preventDefault();
        try{
            await api("/paymnt/refund/"+document.getElementById("paymentId").value,{method:"PATCH"});
            toast("Refund saved successfully");
            setTimeout(()=>location.href="index.html",500);
        }catch(err){toast(err.message);}
    });
});
