package atf.api.reporting.runtime.core;

/** Parsing and normalization rules for duration labels rendered by the report. */
final class ExtentReportDurationParsing {
    private ExtentReportDurationParsing() { }

    static String script() {
        return "function atfDurationValue(raw){var value=String(raw||'').trim(),long=value.match(/^(\\d{2}):(\\d{2}):(\\d{2}):(\\d{3})(?:\\s*ms)?$/i),short=value.match(/^(\\d{2}):(\\d{2}):(\\d{3})(?:\\s*ms)?$/i),millis=value.match(/^(\\d+(?:\\.\\d+)?)\\s*ms$/i),seconds=value.match(/^(\\d+(?:\\.\\d+)?)\\s*s$/i),plain=value.match(/^\\d+(?:\\.\\d+)?$/);if(long)return Number(long[1])*3600000+Number(long[2])*60000+Number(long[3])*1000+Number(long[4]);if(short)return Number(short[1])*60000+Number(short[2])*1000+Number(short[3]);if(seconds)return Number(seconds[1])*1000;if(millis)return Number(millis[1]);return plain?Number(value):null;}"
                + "function atfFormatDuration(ms){var value=Number(ms);if(!Number.isFinite(value)||value<0)return '';if(value<=300)return String(value)+' ms';var seconds=value/1000,textValue=seconds.toFixed(3).replace(/0+$/,'').replace(/\\.$/,'');return textValue+' s';}"
                + "function atfNormalizeDuration(e,source){if(!e)return;var raw=String(e.textContent||'').trim(),ms=atfDurationValue(raw);if(ms===null){if(raw===''||!(/^(pass|fail|warning|skip)$/i.test(raw)))e.textContent='—';e.setAttribute('data-duration-source','absent');return;}e.textContent=atfFormatDuration(ms);e.setAttribute('data-duration-ms',String(ms));e.setAttribute('data-duration-source',source||'measured');}"
                + "function normalizeTestNavigatorSeparators(){q('.test-view .test-list-item .test-detail .text-sm').forEach(function(row){Array.prototype.slice.call(row.childNodes).forEach(function(node){if(node.nodeType===3&&/^\\s*\\/\\s*$/.test(node.nodeValue||''))node.remove();});});}"
                + "function normalizeHeaderMetadata(){q('.test-view .test-content-detail .detail-head .info').forEach(function(info){var anchor=info.querySelector(':scope > .uri-anchor');if(!anchor)return;var separator=anchor.previousSibling;if(separator&&separator.nodeType===3&&/^\\s*·\\s*$/.test(separator.nodeValue||''))separator.remove();});}"
                + "function normalizeDurations(){q(" + ExtentReportRuntimeVocabulary.js(ExtentReportRuntimeVocabulary.Selector.DURATION_MARKERS.value()) + ").forEach(function(e){atfNormalizeDuration(e,'measured');});q('.test-view .test-content-detail .card-toolbar .badge,.category-view .test-content-detail .card-toolbar .badge').forEach(function(e){var raw=String(e.textContent||'').trim(),ms=atfDurationValue(raw);if(!raw||/^(pass|fail|warning|skip)$/i.test(raw)||ms===null)return;e.textContent=atfFormatDuration(ms);e.setAttribute('data-duration-ms',String(ms));e.setAttribute('data-duration-source','measured');});normalizeTestNavigatorSeparators();normalizeHeaderMetadata();}";
    }
}
