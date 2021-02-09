respcs.Nsim       = zeros(1,numel(respcs.EsN0dB));
respcs.NErrActual = zeros(1,numel(respcs.EsN0dB));
respcs.NErrCS     = zeros(1,numel(respcs.EsN0dB));
respcs.NErrMissed = zeros(1,numel(respcs.EsN0dB));
respcs.NErrFalse  = zeros(1,numel(respcs.EsN0dB));
ressum.Nsim        = zeros(1,numel(respcs.EsN0dB));
ressum.NErrActual  = zeros(1,numel(respcs.EsN0dB));
ressum.NErrCS      = zeros(1,numel(respcs.EsN0dB));
ressum.NErrMissed  = zeros(1,numel(respcs.EsN0dB));
ressum.NErrFalse   = zeros(1,numel(respcs.EsN0dB));
rescrc.Nsim       = zeros(1,numel(rescrc.EsN0dB));
rescrc.NErrActual = zeros(1,numel(rescrc.EsN0dB));
rescrc.NErrCS     = zeros(1,numel(rescrc.EsN0dB));
rescrc.NErrMissed = zeros(1,numel(rescrc.EsN0dB));
rescrc.NErrFalse  = zeros(1,numel(rescrc.EsN0dB));
resflet.Nsim       = zeros(1,numel(resflet.EsN0dB));
resflet.NErrActual = zeros(1,numel(resflet.EsN0dB));
resflet.NErrCS     = zeros(1,numel(resflet.EsN0dB));
resflet.NErrMissed = zeros(1,numel(resflet.EsN0dB));
resflet.NErrFalse  = zeros(1,numel(resflet.EsN0dB));

if exist('results.mat', 'file') == 2
    load('results.mat','respcs','ressum','rescrc','resflet')
end