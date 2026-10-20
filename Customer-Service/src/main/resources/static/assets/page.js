document.addEventListener("DOMContentLoaded",()=>{
const base="/customer";
const count=document.getElementById("recordCount");
const rows=document.getElementById("rows");
const search=document.getElementById("search");
let records=[];
async function load(){
 if(!rows)return;
 try{const data=unwrap(await api(base+"/view-all"));records=Array.isArray(data)?data:[];if(count)count.textContent=records.length;render();}
 catch(e){rows.innerHTML='<tr><td colspan="7" class="empty">'+e.message+'</td></tr>';}
}
function bindActions(){
 if(!rows)return;
 rows.querySelectorAll(".row-delete").forEach(button=>button.addEventListener("click",async()=>{
  if(!confirm("Confirm this action?"))return;
  try{await api(base+"/delete/"+button.dataset.id,{method:"DELETE"});toast("Customer updated");await load();}
  catch(e){toast(e.message);}
 }));
}
function render(){
 if(!rows)return;
 const q=(search?.value||"").toLowerCase();
 const filtered=records.filter(x=>JSON.stringify(x).toLowerCase().includes(q));
 rows.innerHTML=filtered.length?filtered.map(x=>`<tr><td>${x.custommerId ?? x.customerId}</td><td>${x.customerName}</td><td>${x.email}</td><td>${x.mobile}</td><td><span class="badge">${x.memberShipType||"REGULAR"}</span></td><td><span class="badge green">${x.status}</span></td><td><button class="btn danger row-delete" data-id="${x.custommerId ?? x.customerId}">Delete</button></td></tr>`).join(""):'<tr><td colspan="7" class="empty">No records found.</td></tr>';
 bindActions();
}
if(search)search.addEventListener("input",render);
if(rows||count)load();
const form=document.getElementById("recordForm");
if(form)form.addEventListener("submit",async event=>{
 event.preventDefault();
 const button=document.getElementById("saveButton");
 const message=document.getElementById("formMessage");
 button.disabled=true;message.textContent="Saving…";
 try{await api(base+"/add",{method:"POST",body:JSON.stringify({customerName:document.getElementById("customerName").value.trim(),email:document.getElementById("email").value.trim(),mobile:document.getElementById("mobile").value.trim(),city:document.getElementById("city").value.trim()})});message.textContent="Saved successfully.";toast("Customer saved successfully");setTimeout(()=>location.href="list.html",500);}
 catch(error){message.textContent=error.message;button.disabled=false;}
});
});