const tabs=[...document.querySelectorAll('.tab')];const panels=[...document.querySelectorAll('.tab-panel')];function activate(id){tabs.forEach(t=>t.classList.toggle('active',t.dataset.tab===id));panels.forEach(p=>p.classList.toggle('active',p.id===id));history.replaceState(null,'','#'+id)}tabs.forEach(t=>t.addEventListener('click',()=>activate(t.dataset.tab)));activate('overview');document.querySelectorAll('.fail-row').forEach(a=>a.addEventListener('click',()=>activate('tests')));





