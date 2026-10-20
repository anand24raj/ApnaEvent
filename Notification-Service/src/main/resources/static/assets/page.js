document.addEventListener("DOMContentLoaded",()=>{
    const button=document.getElementById("loadNotifications");
    const box=document.getElementById("notificationRows");
    if(!button)return;
    button.addEventListener("click",async()=>{
        const id=document.getElementById("customerId").value;
        if(!id){toast("Enter a customer ID");return;}
        try{
            const data=unwrap(await api("/notification/customer/"+id));
            const rows=Array.isArray(data)?data:[];
            if(!rows.length){box.innerHTML='<div class="empty">No notifications found.</div>';return;}
            box.innerHTML=rows.map(n=>`<div class="notice"><strong>${n.notificationType||"Notification"}</strong><div class="muted">${n.message||""}</div><span class="badge">${n.notificationStatus||"PENDING"}</span></div>`).join("");
        }catch(e){box.textContent=e.message;}
    });
});
