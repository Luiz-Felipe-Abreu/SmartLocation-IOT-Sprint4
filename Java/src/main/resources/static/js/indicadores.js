(function(){
  const ENDPOINT = '/api/indicadores';
  const INTERVALO_MS = 7000; // 7s para evitar muitas conexões

  function qs(sel){ return document.querySelector(sel); }

  function atualizarNavbarBadge(alertas){
    const badge = qs('#motosBadge');
    if(!badge) return;
    if(alertas > 0){
      badge.textContent = alertas;
      badge.style.display = 'inline-block';
      badge.classList.add('bg-danger');
      badge.setAttribute('title', alertas + ' alerta(s) ativo(s)');
    } else {
      badge.textContent = '';
      badge.style.display = 'none';
    }
  }

  function renderPainelIndicadores(data){
    const container = qs('#indicadores');
    if(!container) return;

    container.innerHTML = `
      <div class="col-md-2">
        <div class="card stat-card text-center p-3">
          <div class="small text-muted">Total</div>
          <div class="fs-3 fw-bold">${data.totalMotos}</div>
        </div>
      </div>
      <div class="col-md-2">
        <div class="card stat-card text-center p-3">
          <div class="small text-muted">Disponível</div>
          <div class="fs-3 text-success fw-bold">${data.disponivel}</div>
        </div>
      </div>
      <div class="col-md-2">
        <div class="card stat-card text-center p-3">
          <div class="small text-muted">Em uso</div>
          <div class="fs-3 text-primary fw-bold">${data.emUso}</div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card stat-card text-center p-3">
          <div class="small text-muted">Manutenção</div>
          <div class="fs-3 text-warning fw-bold">${data.manutencao}</div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card stat-card text-center p-3">
          <div class="small text-muted">Inativa</div>
          <div class="fs-3 text-secondary fw-bold">${data.inativa}</div>
        </div>
      </div>
      <div class="col-12 mt-3">
        <div class="alert ${data.alertasAtivos>0?'alert-danger':'alert-success'}" role="alert">
          ${data.alertasAtivos>0 ? (data.alertasAtivos + ' alerta(s) ativo(s) em Motos') : 'Nenhum alerta ativo'}
        </div>
      </div>`;
  }

  async function fetchIndicadores(){
    try{
      const res = await fetch(ENDPOINT, {cache: 'no-store'});
      if(!res.ok) throw new Error('HTTP ' + res.status);
      const data = await res.json();
      const alertas = (data.manutencao||0) + (data.inativa||0);
      atualizarNavbarBadge(alertas);
      renderPainelIndicadores({...data, alertasAtivos: alertas});
    }catch(err){
      // Em erro, não derrubar a página; esconder badge
      atualizarNavbarBadge(0);
    }
  }

  // Chamada inicial e polling
  document.addEventListener('DOMContentLoaded', function(){
    fetchIndicadores();
    setInterval(fetchIndicadores, INTERVALO_MS);
  });
})();
