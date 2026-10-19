function normalizeTagTableHeaders(){q('.dashboard-view table th').forEach(function(th){if(String(th.textContent||'').trim().toUpperCase()==='OTHERS')th.textContent='WARNING';});}
function normalizeNativeSummaryLabels(){q('.dashboard-view .card-footer small').forEach(function(s){if(String(s.textContent||'').toLowerCase().indexOf('others')>=0)s.innerHTML=s.innerHTML.replace(/\bothers\b/gi,'warnings');});}
function normalizeCategorySummaryLabels(){q('.category-view .test-contents .info').forEach(function(info){info.innerHTML=info.innerHTML.replace(/\bothers\b/gi,'warnings');});}
